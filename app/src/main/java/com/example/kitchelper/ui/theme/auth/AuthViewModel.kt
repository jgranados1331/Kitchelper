package com.example.kitchenper.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kitchelper.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. Definimos el estado de la UI de autenticación
data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null // Para mensajes como "Correo enviado"
)

// ui/auth/AuthViewModel.kt (agrega arriba de la clase)

data class RegisterData(
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fechaNacimiento: String = "", // Formato "dd/MM/yyyy"
    val experticia: String = "Principiante", // Principiante, Intermedio, Avanzado
    val alergias: List<String> = emptyList(),
    val tipoComida: String = "Variada" // Saludable, Rápida, Gourmet, Variada
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    // 2. Estado interno (mutable) y estado expuesto (inmutable)
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // 3. Función para iniciar sesión
    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState(errorMessage = "Por favor llena todos los campos")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            val result = repository.login(email.trim(), pass)
            result.onSuccess {
                _authState.value = AuthState(isSuccess = true)
            }.onFailure { exception ->
                _authState.value = AuthState(
                    errorMessage = traducirError(exception.message)
                )
            }
        }
    }

    // 4. Función para registrar un nuevo usuario
    fun register(data: RegisterData) {
        // Validaciones
        if (data.email.isBlank() || data.password.isBlank()) {
            _authState.value = AuthState(errorMessage = "Por favor llena todos los campos")
            return
        }
        if (data.password != data.confirmPassword) {
            _authState.value = AuthState(errorMessage = "Las contraseñas no coinciden")
            return
        }
        if (data.password.length < 6) {
            _authState.value = AuthState(errorMessage = "La contraseña debe tener al menos 6 caracteres")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            val result = repository.register(data.email.trim(), data.password)
            result.onSuccess { user ->
                // TODO: Aquí guardaremos los datos extra en Firestore en el siguiente paso
                // Por ahora solo autenticamos
                _authState.value = AuthState(isSuccess = true)
            }.onFailure { exception ->
                _authState.value = AuthState(
                    errorMessage = traducirError(exception.message)
                )
            }
        }
    }

    // 6. Función para limpiar errores y mensajes
    fun clearMessages() {
        _authState.value = _authState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    // 7. Función para resetear el estado de éxito (útil al navegar)
    fun resetSuccess() {
        _authState.value = _authState.value.copy(isSuccess = false)
    }

    // 8. Traducir errores de Firebase al español
    private fun traducirError(mensaje: String?): String {
        return when {
            mensaje == null -> "Ocurrió un error desconocido"
            mensaje.contains("password is invalid", ignoreCase = true) -> "Contraseña incorrecta"
            mensaje.contains("no user record", ignoreCase = true) -> "No existe una cuenta con este correo"
            mensaje.contains("email address is already in use", ignoreCase = true) -> "Este correo ya está registrado"
            mensaje.contains("badly formatted", ignoreCase = true) -> "El correo no tiene un formato válido"
            mensaje.contains("network error", ignoreCase = true) -> "Error de conexión. Revisa tu internet"
            mensaje.contains("too many requests", ignoreCase = true) -> "Demasiados intentos. Intenta más tarde"
            else -> mensaje
        }
    }
}