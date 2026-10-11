package com.example.kitchelper.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.kitchelper.data.model.PracticaBasica
import kotlinx.coroutines.tasks.await

class PracticeRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun obtenerPracticas(): Result<List<PracticaBasica>> {
        return try {
            val snapshot = db.collection("practicas_basicas").get().await()
            val practicas = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PracticaBasica::class.java)?.copy(id = doc.id)
            }
            Result.success(practicas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerPractica(practicaId: String): Result<PracticaBasica?> {
        return try {
            val doc = db.collection("practicas_basicas").document(practicaId).get().await()
            val practica = doc.toObject(PracticaBasica::class.java)?.copy(id = doc.id)
            Result.success(practica)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun crearPractica(practica: PracticaBasica): Result<String> {
        return try {
            val docRef = db.collection("practicas_basicas").document()
            val practicaConId = practica.copy(id = docRef.id)
            docRef.set(practicaConId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
