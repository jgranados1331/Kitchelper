package com.example.kitchelper.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.kitchelper.data.model.Receta
import kotlinx.coroutines.tasks.await

class RecipeRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    // Obtener todas las recetas de Firestore
    suspend fun obtenerRecetas(): Result<List<Receta>> {
        return try {
            val snapshot = db.collection("recetas").get().await()
            val recetas = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Receta::class.java)?.copy(id = doc.id)
            }
            Result.success(recetas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener una receta por ID
    suspend fun obtenerReceta(recetaId: String): Result<Receta?> {
        return try {
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
            val snapshot = db.collection("recetas")
                .whereEqualTo("dificultad", dificultad)
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

    // Obtener recetas con AR
    suspend fun obtenerRecetasConAR(): Result<List<Receta>> {
        return try {
            val snapshot = db.collection("recetas")
                .whereEqualTo("ar.tieneAR", true)
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

    // Crear una nueva receta
    suspend fun crearReceta(receta: Receta): Result<String> {
        return try {
            val docRef = db.collection("recetas").document()
            val recetaConId = receta.copy(id = docRef.id)
            docRef.set(recetaConId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener recetas de un usuario específico
    suspend fun obtenerRecetasDeUsuario(userId: String): Result<List<Receta>> {
        return try {
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
