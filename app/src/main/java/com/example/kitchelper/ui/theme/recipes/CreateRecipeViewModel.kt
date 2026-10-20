package com.example.kitchelper.ui.theme.recipes

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.example.kitchelper.data.model.ARData
import com.example.kitchelper.data.model.Ingrediente
import com.example.kitchelper.data.model.Paso
import com.example.kitchelper.data.model.PasoPractica
import com.example.kitchelper.data.model.PracticaBasica
import com.example.kitchelper.data.model.Receta
import com.example.kitchelper.data.repository.ImageKitRepository
import com.example.kitchelper.data.repository.PracticeRepository
import com.example.kitchelper.data.repository.RecipeRepository
import com.example.kitchelper.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class IngredienteItemState(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String = "",
    val cantidad: String = "",
    val imagenUrl: String = ""
)

data class PasoItemState(
    val id: String = UUID.randomUUID().toString(),
    val orden: Int = 1,
    val descripcion: String = "",
    val imagenUrl: String = "",
    val videoUrl: String = ""
)

data class CreateRecipeState(
    val editingRecipeId: String? = null,
    val esPracticaBasica: Boolean = false, // Toggle entre Receta y Práctica Básica
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "Desayuno",
    val dificultad: String = "Principiante",
    val tiempoMinutosText: String = "15",
    val imagenPrincipalUrl: String = "",
    val videoPrincipalUrl: String = "",
    val ingredientes: List<IngredienteItemState> = listOf(IngredienteItemState()),
    val pasos: List<PasoItemState> = listOf(PasoItemState(orden = 1)),
    val incluyeAR: Boolean = false,
    val instruccionAR: String = "",
    val modelo3DUrlAR: String = "",
    val tagsText: String = "",
    val isUploading: Boolean = false,
    val uploadProgressMessage: String? = null,
    val isSaving: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null
)

class CreateRecipeViewModel(
    private val recipeRepo: RecipeRepository = RecipeRepository(),
    private val practiceRepo: PracticeRepository = PracticeRepository(),
    private val userRepo: UserRepository = UserRepository(),
    private val imageKitRepo: ImageKitRepository = ImageKitRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(CreateRecipeState())
    val state: StateFlow<CreateRecipeState> = _state.asStateFlow()

    fun cargarParaEditar(recetaId: String) {
        if (recetaId.isBlank()) return
        if (_state.value.editingRecipeId == recetaId) return   // ← NUEVO: ya está cargada
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            val result = recipeRepo.obtenerReceta(recetaId)
            result.onSuccess { receta ->
                if (receta != null) {
                    _state.value = CreateRecipeState(
                        editingRecipeId = receta.id,
                        esPracticaBasica = false,
                        titulo = receta.titulo,
                        descripcion = receta.descripcion,
                        categoria = receta.categoria.ifBlank { "Desayuno" },
                        dificultad = receta.dificultad.ifBlank { "Principiante" },
                        tiempoMinutosText = receta.tiempoMinutos.toString(),
                        imagenPrincipalUrl = receta.imagenUrl,
                        videoPrincipalUrl = receta.videoUrl,
                        ingredientes = receta.ingredientes.map {
                            IngredienteItemState(nombre = it.nombre, cantidad = it.cantidad, imagenUrl = it.imagenUrl)
                        }.ifEmpty { listOf(IngredienteItemState()) },
                        pasos = receta.pasos.map {
                            PasoItemState(orden = it.orden, descripcion = it.descripcion, imagenUrl = it.imagenUrl, videoUrl = it.videoUrl)
                        }.ifEmpty { listOf(PasoItemState(orden = 1)) },
                        incluyeAR = receta.ar?.tieneAR == true,
                        instruccionAR = receta.ar?.instruccionAR ?: "",
                        modelo3DUrlAR = receta.ar?.modelo3DUrl ?: "",
                        tagsText = receta.tags.joinToString(", ")
                    )
                } else {
                    _state.value = _state.value.copy(isSaving = false)
                }
            }.onFailure {
                _state.value = _state.value.copy(isSaving = false)
            }
        }
    }

    // ===== Toggle Tipo de Creación (Receta vs Práctica) =====
    fun toggleEsPracticaBasica(esPractica: Boolean) {
        _state.value = _state.value.copy(esPracticaBasica = esPractica)
    }

    // ===== Actualización de datos principales =====
    fun actualizarTitulo(v: String) { _state.value = _state.value.copy(titulo = v) }
    fun actualizarDescripcion(v: String) { _state.value = _state.value.copy(descripcion = v) }
    fun actualizarCategoria(v: String) { _state.value = _state.value.copy(categoria = v) }
    fun actualizarDificultad(v: String) { _state.value = _state.value.copy(dificultad = v) }
    fun actualizarTiempo(v: String) { _state.value = _state.value.copy(tiempoMinutosText = v) }
    fun actualizarVideoPrincipal(v: String) { _state.value = _state.value.copy(videoPrincipalUrl = v) }
    fun actualizarTagsText(v: String) { _state.value = _state.value.copy(tagsText = v) }

    // ===== Actualización de AR =====
    fun toggleIncluyeAR(v: Boolean) { _state.value = _state.value.copy(incluyeAR = v) }
    fun actualizarInstruccionAR(v: String) { _state.value = _state.value.copy(instruccionAR = v) }

    // ===== Subida de imágenes con ImageKit.io =====

    fun subirImagenPrincipal(context: Context, uri: Uri) {
        val folder = if (_state.value.esPracticaBasica) "/Kitchelper/Practicas" else "/Kitchelper/Recetas"
        viewModelScope.launch {
            _state.value = _state.value.copy(isUploading = true, uploadProgressMessage = "Subiendo portada a ImageKit ($folder)...")
            val result = imageKitRepo.subirArchivo(context, uri, "portada", folder)
            result.onSuccess { url ->
                _state.value = _state.value.copy(
                    isUploading = false,
                    uploadProgressMessage = null,
                    imagenPrincipalUrl = url,
                    mensajeExito = "Imagen principal subida con éxito"
                )
            }.onFailure { err ->
                _state.value = _state.value.copy(
                    isUploading = false,
                    uploadProgressMessage = null,
                    mensajeError = "Error al subir imagen principal: ${err.message}"
                )
            }
        }
    }

    fun subirImagenIngrediente(context: Context, uri: Uri, ingredienteId: String) {
        val folder = if (_state.value.esPracticaBasica) "/Kitchelper/Practicas/Ingredientes" else "/Kitchelper/Recetas/Ingredientes"
        viewModelScope.launch {
            _state.value = _state.value.copy(isUploading = true, uploadProgressMessage = "Subiendo ingrediente a ImageKit ($folder)...")
            val result = imageKitRepo.subirArchivo(context, uri, "ingrediente", folder)
            result.onSuccess { url ->
                val listaActualizada = _state.value.ingredientes.map { item ->
                    if (item.id == ingredienteId) item.copy(imagenUrl = url) else item
                }
                _state.value = _state.value.copy(
                    isUploading = false,
                    uploadProgressMessage = null,
                    ingredientes = listaActualizada,
                    mensajeExito = "Imagen de ingrediente subida"
                )
            }.onFailure { err ->
                _state.value = _state.value.copy(
                    isUploading = false,
                    uploadProgressMessage = null,
                    mensajeError = "Error al subir imagen de ingrediente: ${err.message}"
                )
            }
        }
    }

    fun subirImagenPaso(context: Context, uri: Uri, pasoId: String) {
        val folder = if (_state.value.esPracticaBasica) "/Kitchelper/Practicas/Pasos" else "/Kitchelper/Recetas/Pasos"
        viewModelScope.launch {
            _state.value = _state.value.copy(isUploading = true, uploadProgressMessage = "Subiendo paso a ImageKit ($folder)...")
            val result = imageKitRepo.subirArchivo(context, uri, "paso", folder)
            result.onSuccess { url ->
                val listaActualizada = _state.value.pasos.map { item ->
                    if (item.id == pasoId) item.copy(imagenUrl = url) else item
                }
                _state.value = _state.value.copy(
                    isUploading = false,
                    uploadProgressMessage = null,
                    pasos = listaActualizada,
                    mensajeExito = "Imagen del paso subida"
                )
            }.onFailure { err ->
                _state.value = _state.value.copy(
                    isUploading = false,
                    uploadProgressMessage = null,
                    mensajeError = "Error al subir imagen del paso: ${err.message}"
                )
            }
        }
    }

    fun subirModelo3DAR(context: Context, uri: Uri) {
        val folder = if (_state.value.esPracticaBasica) "/Kitchelper/Practicas" else "/Kitchelper/Recetas"
        viewModelScope.launch {
            _state.value = _state.value.copy(isUploading = true, uploadProgressMessage = "Subiendo modelo 3D (.glb) a ImageKit...")
            val result = imageKitRepo.subirArchivo(context, uri, "modelo_3d_ar", folder)
            result.onSuccess { url ->
                _state.value = _state.value.copy(
                    isUploading = false,
                    uploadProgressMessage = null,
                    modelo3DUrlAR = url,
                    mensajeExito = "Modelo 3D cargado con éxito"
                )
            }.onFailure { err ->
                _state.value = _state.value.copy(
                    isUploading = false,
                    uploadProgressMessage = null,
                    mensajeError = "Error al subir modelo 3D: ${err.message}"
                )
            }
        }
    }

    // ===== Gestión de Ingredientes =====

    fun agregarIngrediente() {
        val lista = _state.value.ingredientes + IngredienteItemState()
        _state.value = _state.value.copy(ingredientes = lista)
    }

    fun eliminarIngrediente(id: String) {
        if (_state.value.ingredientes.size > 1) {
            val lista = _state.value.ingredientes.filter { it.id != id }
            _state.value = _state.value.copy(ingredientes = lista)
        }
    }

    fun actualizarIngrediente(id: String, nombre: String, cantidad: String) {
        val lista = _state.value.ingredientes.map {
            if (it.id == id) it.copy(nombre = nombre, cantidad = cantidad) else it
        }
        _state.value = _state.value.copy(ingredientes = lista)
    }

    // ===== Ingredientes escaneados con AR =====
    fun agregarIngredientesDesdeAR(nombres: List<String>) {
        // Quitamos los items en blanco (el estado inicial trae uno vacío)
        val actuales = _state.value.ingredientes.filter { it.nombre.isNotBlank() }
        val existentes = actuales.map { it.nombre.trim().lowercase() }.toSet()

        val nuevos = nombres
            .distinctBy { it.trim().lowercase() }
            .filter { it.trim().lowercase() !in existentes }
            .map { IngredienteItemState(nombre = it.trim()) }

        _state.value = _state.value.copy(
            ingredientes = (actuales + nuevos).ifEmpty { listOf(IngredienteItemState()) },
            mensajeExito = if (nuevos.isNotEmpty())
                "${nuevos.size} ingrediente(s) añadido(s) desde AR"
            else
                "Esos ingredientes ya estaban en la lista"
        )
    }

    // ===== Gestión de Pasos =====

    fun agregarPaso() {
        val nuevoOrden = _state.value.pasos.size + 1
        val lista = _state.value.pasos + PasoItemState(orden = nuevoOrden)
        _state.value = _state.value.copy(pasos = lista)
    }

    fun eliminarPaso(id: String) {
        if (_state.value.pasos.size > 1) {
            val filtrada = _state.value.pasos.filter { it.id != id }
            val reordenada = filtrada.mapIndexed { index, item -> item.copy(orden = index + 1) }
            _state.value = _state.value.copy(pasos = reordenada)
        }
    }

    fun actualizarPaso(id: String, descripcion: String, videoUrl: String) {
        val lista = _state.value.pasos.map {
            if (it.id == id) it.copy(descripcion = descripcion, videoUrl = videoUrl) else it
        }
        _state.value = _state.value.copy(pasos = lista)
    }

    // ===== Publicar o Actualizar Receta o Práctica Básica en Firestore =====

    fun guardarReceta() {
        val st = _state.value

        if (st.titulo.isBlank()) {
            _state.value = st.copy(mensajeError = "Por favor ingresa un título")
            return
        }
        if (st.descripcion.isBlank()) {
            _state.value = st.copy(mensajeError = "Por favor ingresa una descripción")
            return
        }

        // Verificación estricta de sesión de usuario de Firebase Auth
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || currentUser.uid.isBlank()) {
            _state.value = st.copy(mensajeError = "Debes iniciar sesión para publicar o guardar recetas.")
            return
        }
        val userId = currentUser.uid

        val tagsList = st.tagsText.split(",").map { it.trim() }.filter { it.isNotBlank() }

        val arData = if (st.incluyeAR) {
            ARData(
                tieneAR = true,
                instruccionAR = st.instruccionAR,
                modelo3DUrl = st.modelo3DUrlAR
            )
        } else null

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)

            if (st.esPracticaBasica) {
                // Guardar como Práctica Básica
                val pasosPractica = st.pasos.mapIndexedNotNull { index, item ->
                    if (item.descripcion.isNotBlank()) {
                        PasoPractica(orden = index + 1, descripcion = item.descripcion.trim())
                    } else null
                }

                val nuevaPractica = PracticaBasica(
                    titulo = st.titulo.trim(),
                    descripcion = st.descripcion.trim(),
                    tiempoMinutos = st.tiempoMinutosText.toIntOrNull() ?: 15,
                    dificultad = st.dificultad,
                    imagenUrl = st.imagenPrincipalUrl,
                    pasos = pasosPractica,
                    ar = arData
                )

                val result = practiceRepo.crearPractica(nuevaPractica)
                result.onSuccess {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        guardadoExitoso = true,
                        mensajeExito = "¡Práctica básica guardada con éxito!"
                    )
                }.onFailure { err ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        mensajeError = "Error al guardar la práctica: ${err.message}"
                    )
                }
            } else {
                // Guardar o Actualizar como Receta
                val ingredientesFinales = st.ingredientes.mapNotNull {
                    if (it.nombre.isNotBlank()) {
                        Ingrediente(nombre = it.nombre.trim(), cantidad = it.cantidad.trim(), imagenUrl = it.imagenUrl)
                    } else null
                }

                val pasosFinales = st.pasos.mapIndexedNotNull { index, item ->
                    if (item.descripcion.isNotBlank()) {
                        Paso(
                            orden = index + 1,
                            descripcion = item.descripcion.trim(),
                            imagenUrl = item.imagenUrl,
                            videoUrl = item.videoUrl.trim()
                        )
                    } else null
                }

                val userDoc = userRepo.obtenerUsuario(userId).getOrNull()
                val autorNombre = userDoc?.nombre?.takeIf { it.isNotBlank() } ?: "Chef KitchenPer"

                val recetaFinal = Receta(
                    id = st.editingRecipeId ?: "",
                    titulo = st.titulo.trim(),
                    descripcion = st.descripcion.trim(),
                    dificultad = st.dificultad,
                    tiempoMinutos = st.tiempoMinutosText.toIntOrNull() ?: 15,
                    categoria = st.categoria,
                    imagenUrl = st.imagenPrincipalUrl,
                    videoUrl = st.videoPrincipalUrl.trim(),
                    ingredientes = ingredientesFinales,
                    pasos = pasosFinales,
                    tags = tagsList,
                    autorId = userId,
                    autorNombre = autorNombre,
                    esOficial = false,
                    fechaCreacion = System.currentTimeMillis(),
                    ar = arData
                )

                val result = if (st.editingRecipeId != null && st.editingRecipeId.isNotBlank()) {
                    recipeRepo.actualizarReceta(recetaFinal).map { st.editingRecipeId }
                } else {
                    recipeRepo.crearReceta(recetaFinal)
                }

                result.onSuccess {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        guardadoExitoso = true,
                        mensajeExito = if (st.editingRecipeId != null) "¡Receta actualizada con éxito!" else "¡Receta publicada con éxito!"
                    )
                }.onFailure { err ->
                    val msg = if (err.message?.contains("permission", ignoreCase = true) == true) {
                        "Permiso denegado por Firestore. Verifica que tu sesión siga activa."
                    } else err.message ?: "Error desconocido al guardar"
                    _state.value = _state.value.copy(
                        isSaving = false,
                        mensajeError = msg
                    )
                }
            }
        }
    }

    fun limpiarMensajes() {
        _state.value = _state.value.copy(mensajeError = null, mensajeExito = null)
    }
}
