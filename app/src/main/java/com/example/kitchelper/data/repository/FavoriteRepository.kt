package com.example.kitchelper.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.kitchelper.data.model.Favorito
import com.example.kitchelper.data.model.Receta
import kotlinx.coroutines.tasks.await

class FavoriteRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    /**
     * Alternar estado de favorito para un usuario y receta.
     * Devuelve `true` si se agregó a favoritos, o `false` si se removió.
     */
    suspend fun toggleFavorito(userId: String, recetaId: String): Result<Boolean> {
        return try {
            val docId = "${userId}_${recetaId}"
            val docRef = db.collection("favoritos").document(docId)
            val snapshot = docRef.get().await()

            if (snapshot.exists()) {
                // Ya existe -> Eliminar de favoritos
                docRef.delete().await()
                Result.success(false)
            } else {
                // No existe -> Agregar a favoritos
                val favorito = Favorito(
                    id = docId,
                    userId = userId,
                    recetaId = recetaId,
                    fechaAgregado = System.currentTimeMillis()
                )
                docRef.set(favorito).await()
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener el conjunto (Set) de IDs de recetas favoritas del usuario actual.
     */
    suspend fun obtenerIdsFavoritos(userId: String): Result<Set<String>> {
        return try {
            val snapshot = db.collection("favoritos")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val ids = snapshot.documents.mapNotNull { it.getString("recetaId") }.toSet()
            Result.success(ids)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener la lista completa de objetos `Receta` guardados como favoritos por el usuario.
     */
    suspend fun obtenerRecetasFavoritas(userId: String): Result<List<Receta>> {
        return try {
            val snapshotFavs = db.collection("favoritos")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val recetaIds = snapshotFavs.documents.mapNotNull { it.getString("recetaId") }
            if (recetaIds.isEmpty()) {
                return Result.success(emptyList())
            }

            val recetas = mutableListOf<Receta>()
            for (id in recetaIds) {
                val docReceta = db.collection("recetas").document(id).get().await()
                docReceta.toObject(Receta::class.java)?.copy(id = docReceta.id)?.let { receta ->
                    recetas.add(receta)
                }
            }
            Result.success(recetas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
