package com.example.kitchelper.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.example.kitchelper.data.model.InfoCulinaria
import com.example.kitchelper.data.model.Usuario
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    // Obtener el usuario actual (útil para saber si ya inició sesión)
    val currentUser: FirebaseUser? get() = auth.currentUser

    // Función para iniciar sesión con validación de correo verificado
    suspend fun login(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user!!
            
            // Verificar si el correo ha sido verificado
            if (!user.isEmailVerified) {
                auth.signOut()
                return Result.failure(Exception("email_not_verified"))
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Función para registrar un nuevo usuario en Auth y crear su documento en Firestore
    suspend fun register(
        email: String,
        pass: String,
        nombre: String,
        apellido: String,
        fechaNacimiento: String,
        experticia: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user!!

            // Crear el documento inicial del usuario en Firestore vinculado por su UID
            val usuario = Usuario(
                id = user.uid,
                email = email.trim(),
                nombre = nombre.trim(),
                apellido = apellido.trim(),
                fechaNacimiento = fechaNacimiento,
                perfilCompleto = false,
                informacionCulinaria = InfoCulinaria(experticia = experticia)
            )

            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(user.uid)
                .set(usuario)
                .await()
            
            // Enviar correo de verificación de Firebase
            user.sendEmailVerification().await()
            
            // Cerrar sesión para que deba verificar su correo antes de entrar
            auth.signOut()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Función para enviar correo de recuperación de contraseña
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Función para cerrar sesión
    fun logout() {
        auth.signOut()
    }
}
