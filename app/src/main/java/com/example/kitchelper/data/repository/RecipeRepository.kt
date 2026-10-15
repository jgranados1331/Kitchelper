package com.example.kitchelper.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.kitchelper.data.model.Receta
import kotlinx.coroutines.tasks.await

class RecipeRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    // Obtener todas las recetas de Firestore (combina colección pública y subcolección de mis recetas)
    suspend fun obtenerRecetas(): Result<List<Receta>> {
        return try {
            val recetasPublicas = mutableListOf<Receta>()
            try {
                val snapshot = db.collection("recetas").get().await()
                val lista = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Receta::class.java)?.copy(id = doc.id)
                }
                recetasPublicas.addAll(lista)
            } catch (_: Exception) {}

            // Cargar también recetas del usuario desde su subcolección personal
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                try {
                    val userSnap = db.collection("usuarios")
                        .document(currentUser.uid)
                        .collection("mis_recetas")
                        .get()
                        .await()

                    val misRecetas = userSnap.documents.mapNotNull { doc ->
                        doc.toObject(Receta::class.java)?.copy(id = doc.id)
                    }

                    val idsPublicos = recetasPublicas.map { it.id }.toSet()
                    for (rec in misRecetas) {
                        if (!idsPublicos.contains(rec.id)) {
                            recetasPublicas.add(rec)
                        }
                    }
                } catch (_: Exception) {}
            }

            Result.success(recetasPublicas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener una receta por ID
    suspend fun obtenerReceta(recetaId: String): Result<Receta?> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                try {
                    val userDoc = db.collection("usuarios")
                        .document(currentUser.uid)
                        .collection("mis_recetas")
                        .document(recetaId)
                        .get()
                        .await()
                    if (userDoc.exists()) {
                        val recUser = userDoc.toObject(Receta::class.java)?.copy(id = userDoc.id)
                        if (recUser != null) return Result.success(recUser)
                    }
                } catch (_: Exception) {}
            }

            val doc = db.collection("recetas").document(recetaId).get().await()
            val receta = doc.toObject(Receta::class.java)?.copy(id = doc.id)
            Result.success(receta)
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

    // Crear una nueva receta
    suspend fun crearReceta(receta: Receta): Result<String> {
        return try {
            val docRef = if (receta.id.isNotBlank()) db.collection("recetas").document(receta.id) else db.collection("recetas").document()
            val recetaConId = receta.copy(id = docRef.id)

            var guardadoExitoso = false

            // 1. Guardar primero en la subcolección del usuario (100% garantizado por reglas de usuario)
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

            // 2. Intentar guardar en la colección pública global de recetas
            try {
                docRef.set(recetaConId).await()
                guardadoExitoso = true
            } catch (e: Exception) {
                // Si la colección pública tiene restricciones pero ya se guardó en la subcolección del usuario
                if (!guardadoExitoso) {
                    throw e
                }
            }

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar una receta existente
    suspend fun actualizarReceta(receta: Receta): Result<Unit> {
        return try {
            if (receta.id.isBlank()) return Result.failure(Exception("ID de receta no válido"))

            var guardadoExitoso = false

            // 1. Actualizar en la subcolección del usuario
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

            // 2. Actualizar en la colección pública
            try {
                db.collection("recetas").document(receta.id).set(receta).await()
                guardadoExitoso = true
            } catch (e: Exception) {
                if (!guardadoExitoso) throw e
            }

            Result.success(Unit)
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

    // Obtener recetas de un usuario específico
    suspend fun obtenerRecetasDeUsuario(userId: String): Result<List<Receta>> {
        return try {
            val userSnap = db.collection("usuarios")
                .document(userId)
                .collection("mis_recetas")
                .get()
                .await()

            val recetasUser = userSnap.documents.mapNotNull { doc ->
                doc.toObject(Receta::class.java)?.copy(id = doc.id)
            }

            if (recetasUser.isNotEmpty()) {
                return Result.success(recetasUser)
            }

            val snapshot = db.collection("recetas")
                .whereEqualTo("autorId", userId)
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
