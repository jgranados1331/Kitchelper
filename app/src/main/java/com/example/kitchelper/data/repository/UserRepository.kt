package com.example.kitchelper.data.repository

import com.google.firebase.auth.FirebaseAuth
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
     * Eliminar completamente los datos del usuario de Firestore y su cuenta de Firebase Auth.
     */
    suspend fun eliminarCuentaUsuario(userId: String): Result<Unit> {
        return try {
            // 1. Eliminar favoritos de la subcolección del usuario
            val favsSnapshot = db.collection("usuarios")
                .document(userId)
                .collection("favoritos")
                .get()
                .await()

            for (doc in favsSnapshot.documents) {
                doc.reference.delete().await()
            }

            // 2. Eliminar el documento de usuario principal en Firestore
            db.collection("usuarios").document(userId).delete().await()

            // 3. Eliminar la cuenta de autenticación de Firebase
            val authUser = FirebaseAuth.getInstance().currentUser
            if (authUser != null && authUser.uid == userId) {
                authUser.delete().await()
            } else {
                FirebaseAuth.getInstance().signOut()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            // Si la sesión es antigua y requiere re-autenticación por seguridad
            if (e.message?.contains("requires-recent-login", ignoreCase = true) == true) {
                FirebaseAuth.getInstance().signOut()
                Result.failure(Exception("Por razones de seguridad, debes volver a iniciar sesión antes de eliminar tu cuenta."))
            } else {
                Result.failure(e)
            }
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
