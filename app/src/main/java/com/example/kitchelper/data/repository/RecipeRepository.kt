package com.example.kitchelper.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.kitchelper.data.model.Receta
import kotlinx.coroutines.tasks.await

class RecipeRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    // Obtener todas las recetas globales creadas por cualquier usuario en la plataforma
    suspend fun obtenerRecetas(): Result<List<Receta>> {
        return try {
            val recetasMapa = mutableMapOf<String, Receta>()

            // 1. Obtener de la colección pública principal "recetas"
            try {
                val snapshotPublic = db.collection("recetas").get().await()
                snapshotPublic.documents.mapNotNull { doc ->
                    doc.toObject(Receta::class.java)?.copy(id = doc.id)
                }.forEach { receta ->
                    recetasMapa[receta.id] = receta
                }
            } catch (_: Exception) {}

            // 2. Obtener de todas las subcolecciones "mis_recetas" de todos los usuarios mediante collectionGroup
            try {
                val snapshotGroup = db.collectionGroup("mis_recetas").get().await()
                snapshotGroup.documents.mapNotNull { doc ->
                    doc.toObject(Receta::class.java)?.copy(id = doc.id)
                }.forEach { receta ->
                    if (!recetasMapa.containsKey(receta.id)) {
                        recetasMapa[receta.id] = receta
                    }
                }
            } catch (_: Exception) {}

            Result.success(recetasMapa.values.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener una receta por ID buscando globalmente
    suspend fun obtenerReceta(recetaId: String): Result<Receta?> {
        return try {
            // 1. Buscar en la colección pública principal
            try {
                val doc = db.collection("recetas").document(recetaId).get().await()
                if (doc.exists()) {
                    val receta = doc.toObject(Receta::class.java)?.copy(id = doc.id)
                    if (receta != null) return Result.success(receta)
                }
            } catch (_: Exception) {}

            // 2. Buscar en las subcolecciones mediante collectionGroup
            try {
                val groupSnap = db.collectionGroup("mis_recetas")
                    .get()
                    .await()

                val doc = groupSnap.documents.firstOrNull { it.id == recetaId }
                if (doc != null && doc.exists()) {
                    val receta = doc.toObject(Receta::class.java)?.copy(id = doc.id)
                    if (receta != null) return Result.success(receta)
                }
            } catch (_: Exception) {}

            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Filtrar recetas por dificultad
    suspend fun obtenerRecetasPorDificultad(dificultad: String): Result<List<Receta>> {
        return try {
            val todas = obtenerRecetas().getOrNull() ?: emptyList()
            val filtradas = todas.filter { it.dificultad.equals(dificultad, ignoreCase = true) }
            Result.success(filtradas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener recetas con AR
    suspend fun obtenerRecetasConAR(): Result<List<Receta>> {
        return try {
            val todas = obtenerRecetas().getOrNull() ?: emptyList()
            val filtradas = todas.filter { it.ar?.tieneAR == true }
            Result.success(filtradas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Crear una nueva receta globalmente
    suspend fun crearReceta(receta: Receta): Result<String> {
        return try {
            val docRef = if (receta.id.isNotBlank()) db.collection("recetas").document(receta.id) else db.collection("recetas").document()
            val recetaConId = receta.copy(id = docRef.id)

            var guardadoExitoso = false

            // 1. Guardar en la colección pública global "recetas"
            try {
                docRef.set(recetaConId).await()
                guardadoExitoso = true
            } catch (_: Exception) {}

            // 2. Guardar en la subcolección del usuario
            if (receta.autorId.isNotBlank() && receta.autorId != "anonimo") {
                try {
                    db.collection("usuarios")
                        .document(receta.autorId)
                        .collection("mis_recetas")
                        .document(docRef.id)
                        .set(recetaConId)
                        .await()
                    guardadoExitoso = true
                } catch (_: Exception) {}
            }

            if (guardadoExitoso) {
                Result.success(docRef.id)
            } else {
                Result.failure(Exception("No se pudo guardar la receta en Firestore."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar una receta existente globalmente
    suspend fun actualizarReceta(receta: Receta): Result<Unit> {
        return try {
            if (receta.id.isBlank()) return Result.failure(Exception("ID de receta no válido"))

            var guardadoExitoso = false

            // 1. Actualizar en la colección pública
            try {
                db.collection("recetas").document(receta.id).set(receta).await()
                guardadoExitoso = true
            } catch (_: Exception) {}

            // 2. Actualizar en la subcolección del usuario
            if (receta.autorId.isNotBlank() && receta.autorId != "anonimo") {
                try {
                    db.collection("usuarios")
                        .document(receta.autorId)
                        .collection("mis_recetas")
                        .document(receta.id)
                        .set(receta)
                        .await()
                    guardadoExitoso = true
                } catch (_: Exception) {}
            }

            if (guardadoExitoso) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("No se pudo actualizar la receta."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar una receta de Firestore
    suspend fun eliminarReceta(recetaId: String): Result<Unit> {
        return try {
            if (recetaId.isBlank()) return Result.failure(Exception("ID de receta no válido"))

            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid ?: ""

            if (userId.isNotBlank()) {
                try {
                    db.collection("usuarios")
                        .document(userId)
                        .collection("mis_recetas")
                        .document(recetaId)
                        .delete()
                        .await()
                } catch (_: Exception) {}
            }

            try {
                db.collection("recetas").document(recetaId).delete().await()
            } catch (_: Exception) {}

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener recetas creadas por un usuario específico
    suspend fun obtenerRecetasDeUsuario(userId: String): Result<List<Receta>> {
        return try {
            val todas = obtenerRecetas().getOrNull() ?: emptyList()
            val filtradas = todas.filter { it.autorId == userId }
            Result.success(filtradas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
