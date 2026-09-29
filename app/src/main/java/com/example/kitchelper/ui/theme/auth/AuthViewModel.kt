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
    val loadingMessage: String = "Cargando...",
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null, // Para mensajes como "Correo enviado" o registro exitoso con verificación
    val emailEnviado: String? = null,
    val showNoInternet: Boolean = false
)

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
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _authState.value = AuthState(errorMessage = "El correo no tiene un formato válido")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true, loadingMessage = "Entrando a la cocina")
            val result = repository.login(email.trim(), pass)
            result.onSuccess {
                _authState.value = AuthState(isSuccess = true)
            }.onFailure { exception ->
                val err = traducirError(exception.message)
                _authState.value = AuthState(
                    errorMessage = err,
                    showNoInternet = err.contains("conexión", ignoreCase = true)
                )
            }
        }
    }

    // Validaciones paso a paso para el Registro
    fun validateStep0(nombre: String): Boolean {
        if (nombre.isBlank() || nombre.trim().length < 2) {
            _authState.value = AuthState(errorMessage = "Por favor ingresa un nombre válido (mínimo 2 letras)")
            return false
        }
        return true
    }

    fun validateStep1(apellido: String): Boolean {
        if (apellido.isBlank() || apellido.trim().length < 2) {
            _authState.value = AuthState(errorMessage = "Por favor ingresa un apellido válido (mínimo 2 letras)")
            return false
        }
        return true
    }

    fun validateStep2(email: String, pass: String, confirm: String): Boolean {
        if (email.isBlank() || pass.isBlank() || confirm.isBlank()) {
            _authState.value = AuthState(errorMessage = "Por favor llena todos los campos")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _authState.value = AuthState(errorMessage = "El correo no tiene un formato válido")
            return false
        }
        if (pass != confirm) {
            _authState.value = AuthState(errorMessage = "Las contraseñas no coinciden")
            return false
        }
        if (pass.length < 12) {
            _authState.value = AuthState(errorMessage = "La contraseña debe tener al menos 12 caracteres")
            return false
        }
        if (!pass.contains(Regex("[A-Z]"))) {
            _authState.value = AuthState(errorMessage = "La contraseña debe contener al menos una letra mayúscula")
            return false
        }
        if (!pass.contains(Regex("[0-9]"))) {
            _authState.value = AuthState(errorMessage = "La contraseña debe contener al menos un número")
            return false
        }
        if (!pass.contains(Regex("[^A-Za-z0-9]"))) {
            _authState.value = AuthState(errorMessage = "La contraseña debe contener al menos un carácter especial")
            return false
        }
        return true
    }

    fun validateStep3(dia: String, mes: String, anio: String): Boolean {
        if (dia.isBlank() || mes.isBlank() || anio.isBlank()) {
            _authState.value = AuthState(errorMessage = "Por favor completa tu fecha de nacimiento")
            return false
        }
        val fechaStr = "$dia/$mes/$anio"
        val ageError = validarEdad(fechaStr)
        if (ageError != null) {
            _authState.value = AuthState(errorMessage = ageError)
            return false
        }
        return true
    }

    // 4. Función para registrar un nuevo usuario
    fun register(data: RegisterData) {
        if (!validateStep0(data.nombre) || !validateStep1(data.apellido) ||
            !validateStep2(data.email, data.password, data.confirmPassword) ||
            !validateStep3(data.fechaNacimiento.split("/").getOrElse(0) { "" },
                           data.fechaNacimiento.split("/").getOrElse(1) { "" },
                           data.fechaNacimiento.split("/").getOrElse(2) { "" })) {
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true, loadingMessage = "Creando chef")
            val result = repository.register(data.email.trim(), data.password)
            result.onSuccess { user ->
                // Éxito: se creó el usuario y se envió el correo de verificación.
                // Informamos al usuario que debe verificar su correo antes de iniciar sesión.
                _authState.value = AuthState(
                    successMessage = "¡Cuenta creada con éxito! Te hemos enviado un correo de verificación. Por favor verifícalo en tu bandeja de entrada o spam antes de iniciar sesión."
                )
            }.onFailure { exception ->
                val err = traducirError(exception.message)
                _authState.value = AuthState(
                    errorMessage = err,
                    showNoInternet = err.contains("conexión", ignoreCase = true)
                )
            }
        }
    }

    private fun validarEdad(fechaStr: String): String? {
        try {
            val parts = fechaStr.split("/")
            if (parts.size != 3) return "Fecha de nacimiento inválida (DD/MM/AAAA)"
            val day = parts[0].toIntOrNull() ?: return "Día inválido"
            val month = parts[1].toIntOrNull() ?: return "Mes inválido"
            val year = parts[2].toIntOrNull() ?: return "Año inválido"

            val dob = java.util.Calendar.getInstance().apply {
                set(year, month - 1, day)
            }
            val today = java.util.Calendar.getInstance()

            var age = today.get(java.util.Calendar.YEAR) - dob.get(java.util.Calendar.YEAR)
            if (today.get(java.util.Calendar.DAY_OF_YEAR) < dob.get(java.util.Calendar.DAY_OF_YEAR)) {
                age--
            }

            if (age < 12) {
                return "Debes ser mayor de 12 años para registrarte"
            }
            if (age > 120 || dob.timeInMillis > today.timeInMillis) {
                return "Fecha de nacimiento no válida"
            }
        } catch (_: Exception) {
            return "Formato de fecha no válido"
        }
        return null
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState(errorMessage = "Por favor ingresa tu correo")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState(errorMessage = "El correo no tiene un formato válido")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true, loadingMessage = "Nunca la olvides, ni la compartas")
            val result = repository.resetPassword(email.trim())
            result.onSuccess {
                _authState.value = AuthState(
                    successMessage = "Correo enviado correctamente",
                    emailEnviado = email.trim()
                )
            }.onFailure { exception ->
                val err = traducirError(exception.message)
                _authState.value = AuthState(
                    errorMessage = err,
                    showNoInternet = err.contains("conexión", ignoreCase = true)
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

    // Ocultar banner de internet
    fun dismissNoInternet() {
        _authState.value = _authState.value.copy(showNoInternet = false)
    }

    // 8. Traducir errores de Firebase al español
    private fun traducirError(mensaje: String?): String {
        return when {
            mensaje == null -> "Ocurrió un error desconocido"
            mensaje.contains("email_not_verified", ignoreCase = true) -> "Por favor verifica tu correo electrónico antes de ingresar. Revisa tu bandeja de entrada o spam."
            mensaje.contains("password is invalid", ignoreCase = true) ||
            mensaje.contains("wrong-password", ignoreCase = true) ||
            mensaje.contains("invalid-credential", ignoreCase = true) -> "Contraseña incorrecta"
            mensaje.contains("no user record", ignoreCase = true) ||
            mensaje.contains("user-not-found", ignoreCase = true) -> "No existe una cuenta con este correo"
            mensaje.contains("email address is already in use", ignoreCase = true) -> "Este correo ya está registrado"
            mensaje.contains("badly formatted", ignoreCase = true) -> "El correo no tiene un formato válido"
            mensaje.contains("network error", ignoreCase = true) ||
            mensaje.contains("unable to resolve host", ignoreCase = true) ||
            mensaje.contains("connect timed out", ignoreCase = true) -> "Error de conexión a internet"
            mensaje.contains("too many requests", ignoreCase = true) -> "Demasiados intentos. Intenta más tarde"
            else -> mensaje
        }
    }
}
