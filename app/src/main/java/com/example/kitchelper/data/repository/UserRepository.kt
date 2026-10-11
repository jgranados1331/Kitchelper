package com.example.kitchelper.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.kitchelper.data.model.Usuario
import kotlinx.coroutines.tasks.await

class UserRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun guardarUsuario(usuario: Usuario): Result<Unit> {
        return try {
            db.collection("usuarios")
                .document(usuario.id)
                .set(usuario)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerUsuario(userId: String): Result<Usuario?> {
        return try {
            val doc = db.collection("usuarios").document(userId).get().await()
            val usuario = doc.toObject(Usuario::class.java)
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verifica si el perfil está completo basándose en campos obligatorios.
     * Se usa para actualizar la bandera `perfilCompleto` automáticamente.
     */
    fun esPerfilCompleto(usuario: Usuario): Boolean {
        return usuario.nombre.isNotBlank() &&
                usuario.apellido.isNotBlank() &&
                usuario.telefono.isNotBlank() &&
                usuario.fechaNacimiento.isNotBlank()
    }
}