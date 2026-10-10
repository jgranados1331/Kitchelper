package com.example.kitchelper.data.repository


import com.google.firebase.firestore.FirebaseFirestore
import com.example.kitchelper.data.model.Usuario
import kotlinx.coroutines.tasks.await

class UserRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    // Guardar perfil de usuario
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

    // Obtener perfil de usuario por ID
    suspend fun obtenerUsuario(userId: String): Result<Usuario?> {
        return try {
            val doc = db.collection("usuarios").document(userId).get().await()
            val usuario = doc.toObject(Usuario::class.java)
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar campos específicos (ej. perfilCompleto)
    suspend fun actualizarPerfilCompleto(userId: String, completo: Boolean): Result<Unit> {
        return try {
            db.collection("usuarios")
                .document(userId)
                .update(
                    mapOf(
                        "perfilCompleto" to completo,
                        "ultimaActualizacion" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}