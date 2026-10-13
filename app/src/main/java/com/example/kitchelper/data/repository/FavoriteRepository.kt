package com.example.kitchelper.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.kitchelper.data.model.Favorito
import com.example.kitchelper.data.model.Receta
import kotlinx.coroutines.tasks.await

class FavoriteRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    /**
     * Alternar estado de favorito para un usuario y receta.
     * Guarda la información dentro del árbol del usuario (`usuarios/{userId}/favoritos/{recetaId}`)
     * para garantizar los permisos de Firestore sin errores de "Acceso Denegado".
     */
    suspend fun toggleFavorito(userId: String, recetaId: String): Result<Boolean> {
        return try {
            val userFavRef = db.collection("usuarios")
                .document(userId)
                .collection("favoritos")
                .document(recetaId)

            val snapshot = userFavRef.get().await()

            if (snapshot.exists()) {
                // Ya existe en los favoritos del usuario -> Eliminar
                userFavRef.delete().await()
                try {
                    db.collection("favoritos").document("${userId}_${recetaId}").delete().await()
                } catch (_: Exception) {}
                Result.success(false)
            } else {
                // No existe -> Agregar a los favoritos del usuario
                val favorito = Favorito(
                    id = recetaId,
                    userId = userId,
                    recetaId = recetaId,
                    fechaAgregado = System.currentTimeMillis()
                )
                userFavRef.set(favorito).await()
                try {
                    db.collection("favoritos").document("${userId}_${recetaId}").set(favorito).await()
                } catch (_: Exception) {}
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
            val userFavs = db.collection("usuarios")
                .document(userId)
                .collection("favoritos")
                .get()
                .await()

            val ids = userFavs.documents.map { it.id }.toMutableSet()

            try {
                val rootFavs = db.collection("favoritos")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                rootFavs.documents.mapNotNull { it.getString("recetaId") }.forEach { ids.add(it) }
            } catch (_: Exception) {}

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
            val idsResult = obtenerIdsFavoritos(userId)
            val recetaIds = idsResult.getOrNull() ?: emptySet()
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
