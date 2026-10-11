package com.example.kitchelper.ui.theme.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.example.kitchelper.data.model.Usuario
import com.example.kitchelper.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileState(
    val isLoading: Boolean = true,
    val initialUsuario: Usuario = Usuario(),
    val usuario: Usuario = Usuario(),
    val mensaje: String? = null,
    val guardadoExitoso: Boolean = false,
    val errorMessage: String? = null,
    val showDialogSalirIncompleto: Boolean = false,
    val showDialogCambiosSinGuardar: Boolean = false,
    val showDialogConfirmarGuardar: Boolean = false,
    val pendingNavigationRoute: String? = null,
    val debeNavegar: Boolean = false
) {
    val hayCambiosSinGuardar: Boolean
        get() = usuario != initialUsuario
}

class ProfileViewModel(
    private val userRepo: UserRepository = UserRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        val uid = userId ?: return
        val currentAuthEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = userRepo.obtenerUsuario(uid)
            result.onSuccess { usuario ->
                val baseUsuario = usuario ?: Usuario(id = uid, email = currentAuthEmail)
                val usuarioFinal = if (baseUsuario.email.isBlank() && currentAuthEmail.isNotBlank()) {
                    baseUsuario.copy(email = currentAuthEmail)
                } else baseUsuario

                _state.value = ProfileState(
                    isLoading = false,
                    initialUsuario = usuarioFinal,
                    usuario = usuarioFinal
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar perfil: ${it.message}"
                )
            }
        }
    }

    // ===== Actualización de campos =====

    fun actualizarNombre(valor: String) {
        _state.value = _state.value.copy(usuario = _state.value.usuario.copy(nombre = valor))
    }
    fun actualizarApellido(valor: String) {
        _state.value = _state.value.copy(usuario = _state.value.usuario.copy(apellido = valor))
    }
    fun actualizarEmail(valor: String) {
        _state.value = _state.value.copy(usuario = _state.value.usuario.copy(email = valor))
    }
    fun actualizarTelefono(valor: String) {
        _state.value = _state.value.copy(usuario = _state.value.usuario.copy(telefono = valor))
    }
    fun actualizarFechaNacimiento(valor: String) {
        _state.value = _state.value.copy(usuario = _state.value.usuario.copy(fechaNacimiento = valor))
    }

    fun actualizarInfoCulinaria(infoActualizada: com.example.kitchelper.data.model.InfoCulinaria) {
        _state.value = _state.value.copy(
            usuario = _state.value.usuario.copy(informacionCulinaria = infoActualizada)
        )
    }

    fun actualizarInfoSalud(infoActualizada: com.example.kitchelper.data.model.InfoSalud) {
        _state.value = _state.value.copy(
            usuario = _state.value.usuario.copy(informacionSalud = infoActualizada)
        )
    }

    // ===== Validaciones y Diálogos de Salida / Guardar =====

    fun solicitarSalida(rutaDestino: String? = null) {
        val currentState = _state.value
        if (!currentState.usuario.perfilCompleto) {
            // Alerta 1: Perfil incompleto
            _state.value = currentState.copy(
                showDialogSalirIncompleto = true,
                pendingNavigationRoute = rutaDestino
            )
        } else if (currentState.hayCambiosSinGuardar) {
            // Alerta 2: Perfil completo pero con cambios sin guardar
            _state.value = currentState.copy(
                showDialogCambiosSinGuardar = true,
                pendingNavigationRoute = rutaDestino
            )
        } else {
            // Salir directamente sin alerta
            _state.value = currentState.copy(
                pendingNavigationRoute = rutaDestino,
                debeNavegar = true
            )
        }
    }

    fun confirmarSalida() {
        _state.value = _state.value.copy(
            showDialogSalirIncompleto = false,
            showDialogCambiosSinGuardar = false,
            debeNavegar = true
        )
    }

    fun cancelarSalida() {
        _state.value = _state.value.copy(
            showDialogSalirIncompleto = false,
            showDialogCambiosSinGuardar = false,
            pendingNavigationRoute = null
        )
    }

    fun resetNavegacion() {
        _state.value = _state.value.copy(debeNavegar = false)
    }

    fun solicitarGuardar() {
        _state.value = _state.value.copy(showDialogConfirmarGuardar = true)
    }

    fun cancelarGuardar() {
        _state.value = _state.value.copy(showDialogConfirmarGuardar = false)
    }

    // ===== Guardar perfil =====

    fun guardarPerfil() {
        val uid = userId ?: return
        val usuarioActual = _state.value.usuario.copy(id = uid)

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, showDialogConfirmarGuardar = false)

            // Verificar si el perfil está completo
            val perfilCompleto = userRepo.esPerfilCompleto(usuarioActual)
            val usuarioFinal = usuarioActual.copy(
                perfilCompleto = perfilCompleto,
                ultimaActualizacion = System.currentTimeMillis()
            )

            val result = userRepo.guardarUsuario(usuarioFinal)
            result.onSuccess {
                _state.value = _state.value.copy(
                    isLoading = false,
                    initialUsuario = usuarioFinal,
                    usuario = usuarioFinal,
                    mensaje = "Perfil guardado correctamente",
                    guardadoExitoso = true
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error al guardar: ${it.message}"
                )
            }
        }
    }

    fun limpiarMensajes() {
        _state.value = _state.value.copy(mensaje = null, errorMessage = null, guardadoExitoso = false)
    }
}
