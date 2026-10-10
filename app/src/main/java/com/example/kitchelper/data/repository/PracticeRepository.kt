package com.example.kitchelper.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.kitchelper.data.model.PracticaBasica
import kotlinx.coroutines.tasks.await

class PracticeRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun obtenerPracticas(): Result<List<PracticaBasica>> {
        return try {
            val snapshot = db.collection("practicas_basicas").get().await()
            val practicas = snapshot.toObjects(PracticaBasica::class.java)
            Result.success(practicas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerPractica(practicaId: String): Result<PracticaBasica?> {
        return try {
            val doc = db.collection("practicas_basicas").document(practicaId).get().await()
            val practica = doc.toObject(PracticaBasica::class.java)
            Result.success(practica)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}