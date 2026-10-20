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

            // 1. Obtener de la colección pública principal global "recetas"
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
            // 1. Buscar en la colección pública principal "recetas"
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

    // Crear una nueva receta de manera global en "recetas" y en la subcolección del usuario
    suspend fun crearReceta(receta: Receta): Result<String> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid ?: receta.autorId.takeIf { it.isNotBlank() } ?: "anonimo"
            val docId = if (receta.id.isNotBlank()) receta.id else db.collection("recetas").document().id
            val recetaConId = receta.copy(id = docId, autorId = userId)

            // 1. Guardar en la colección global principal "recetas" (permitido por las nuevas reglas)
            db.collection("recetas")
                .document(docId)
                .set(recetaConId)
                .await()

            // 2. Guardar en la subcolección del usuario ("usuarios/{userId}/mis_recetas/{docId}")
            if (userId.isNotBlank() && userId != "anonimo") {
                try {
                    db.collection("usuarios")
                        .document(userId)
                        .collection("mis_recetas")
                        .document(docId)
                        .set(recetaConId)
                        .await()
                } catch (_: Exception) {}
            }

            Result.success(docId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar una receta existente de manera global y en el usuario (validando autoría)
    suspend fun actualizarReceta(receta: Receta): Result<Unit> {
        return try {
            if (receta.id.isBlank()) return Result.failure(Exception("ID de receta no válido"))

            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid ?: ""

            // Verificar autoría
            if (userId.isNotBlank() && receta.autorId.isNotBlank() && receta.autorId != userId) {
                return Result.failure(Exception("No tienes permisos para modificar la receta de otro usuario."))
            }

            val authorToUse = if (receta.autorId.isNotBlank()) receta.autorId else userId

            // 1. Actualizar en la colección global "recetas"
            db.collection("recetas")
                .document(receta.id)
                .set(receta)
                .await()

            // 2. Actualizar en la subcolección del usuario
            if (authorToUse.isNotBlank() && authorToUse != "anonimo") {
                try {
                    db.collection("usuarios")
                        .document(authorToUse)
                        .collection("mis_recetas")
                        .document(receta.id)
                        .set(receta)
                        .await()
                } catch (_: Exception) {}
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar una receta de Firestore globalmente y del usuario (validando autoría)
    suspend fun eliminarReceta(recetaId: String): Result<Unit> {
        return try {
            if (recetaId.isBlank()) return Result.failure(Exception("ID de receta no válido"))

            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid ?: ""

            // Obtener receta para verificar autoría
            val recetaActual = obtenerReceta(recetaId).getOrNull()
            if (recetaActual != null && userId.isNotBlank() && recetaActual.autorId.isNotBlank() && recetaActual.autorId != userId) {
                return Result.failure(Exception("No tienes permisos para eliminar la receta de otro usuario."))
            }

            val ownerId = recetaActual?.autorId?.takeIf { it.isNotBlank() } ?: userId

            // 1. Eliminar de la subcolección del usuario
            if (ownerId.isNotBlank() && ownerId != "anonimo") {
                try {
                    db.collection("usuarios")
                        .document(ownerId)
                        .collection("mis_recetas")
                        .document(recetaId)
                        .delete()
                        .await()
                } catch (_: Exception) {}
            }

            // 2. Eliminar de la colección global "recetas"
            db.collection("recetas")
                .document(recetaId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener recetas creadas por un usuario específico (desde su subcolección o filtradas)
    suspend fun obtenerRecetasDeUsuario(userId: String): Result<List<Receta>> {
        return try {
            val snapshot = db.collection("usuarios")
                .document(userId)
                .collection("mis_recetas")
                .get()
                .await()
            val recetas = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Receta::class.java)?.copy(id = doc.id)
            }
            Result.success(recetas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
