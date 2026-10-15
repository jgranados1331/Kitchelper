package com.example.kitchelper.ui.theme.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.example.kitchelper.data.model.Receta
import com.example.kitchelper.data.repository.FavoriteRepository
import com.example.kitchelper.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipesState(
    val isLoading: Boolean = true,
    val recetas: List<Receta> = emptyList(),
    val recetasFiltradas: List<Receta> = emptyList(),
    val favoritoIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val categoriaSeleccionada: String = "Todas",
    val dificultadSeleccionada: String = "Todas",
    val soloMisRecetas: Boolean = false,
    val currentUserId: String = "",
    val recetaAEliminarId: String? = null,
    val showDialogConfirmarEliminar: Boolean = false,
    val errorMessage: String? = null,
    val mensajeExito: String? = null
)

class RecipesViewModel(
    private val recipeRepo: RecipeRepository = RecipeRepository(),
    private val favoriteRepo: FavoriteRepository = FavoriteRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(RecipesState())
    val state: StateFlow<RecipesState> = _state.asStateFlow()

    init {
        cargarRecetas()
    }

    fun cargarRecetas() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, currentUserId = userId)

            val result = recipeRepo.obtenerRecetas()
            val favsResult = if (userId.isNotBlank()) favoriteRepo.obtenerIdsFavoritos(userId) else Result.success(emptySet())

            result.onSuccess { lista ->
                val favIds = favsResult.getOrNull() ?: emptySet()
                val current = _state.value
                val filtradas = aplicarFiltros(lista, current.searchQuery, current.categoriaSeleccionada, current.dificultadSeleccionada, current.soloMisRecetas, userId)
                _state.value = current.copy(
                    isLoading = false,
                    recetas = lista,
                    recetasFiltradas = filtradas,
                    favoritoIds = favIds,
                    currentUserId = userId
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al cargar recetas"
                )
            }
        }
    }

    fun toggleSoloMisRecetas(activado: Boolean) {
        val current = _state.value
        val filtradas = aplicarFiltros(current.recetas, current.searchQuery, current.categoriaSeleccionada, current.dificultadSeleccionada, activado, current.currentUserId)
        _state.value = current.copy(soloMisRecetas = activado, recetasFiltradas = filtradas)
    }

    fun toggleFavorito(recetaId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            val result = favoriteRepo.toggleFavorito(userId, recetaId)
            result.onSuccess { fueAgregado ->
                val actuales = _state.value.favoritoIds
                val nuevos = if (fueAgregado) actuales + recetaId else actuales - recetaId
                _state.value = _state.value.copy(favoritoIds = nuevos)
            }
        }
    }

    fun buscarRecetas(query: String) {
        val current = _state.value
        val filtradas = aplicarFiltros(current.recetas, query, current.categoriaSeleccionada, current.dificultadSeleccionada, current.soloMisRecetas, current.currentUserId)
        _state.value = current.copy(searchQuery = query, recetasFiltradas = filtradas)
    }

    fun filtrarPorCategoria(categoria: String) {
        val current = _state.value
        val nuevaCat = if (current.categoriaSeleccionada.equals(categoria, ignoreCase = true)) "Todas" else categoria
        val filtradas = aplicarFiltros(current.recetas, current.searchQuery, nuevaCat, current.dificultadSeleccionada, current.soloMisRecetas, current.currentUserId)
        _state.value = current.copy(categoriaSeleccionada = nuevaCat, recetasFiltradas = filtradas)
    }

    fun filtrarPorDificultad(dificultad: String) {
        val current = _state.value
        val nuevaDif = if (current.dificultadSeleccionada.equals(dificultad, ignoreCase = true)) "Todas" else dificultad
        val filtradas = aplicarFiltros(current.recetas, current.searchQuery, current.categoriaSeleccionada, nuevaDif, current.soloMisRecetas, current.currentUserId)
        _state.value = current.copy(dificultadSeleccionada = nuevaDif, recetasFiltradas = filtradas)
    }

    // ===== Eliminar receta propia =====

    fun solicitarEliminarReceta(recetaId: String) {
        _state.value = _state.value.copy(
            recetaAEliminarId = recetaId,
            showDialogConfirmarEliminar = true
        )
    }

    fun cancelarEliminarReceta() {
        _state.value = _state.value.copy(
            recetaAEliminarId = null,
            showDialogConfirmarEliminar = false
        )
    }

    fun confirmarEliminarReceta() {
        val id = _state.value.recetaAEliminarId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(showDialogConfirmarEliminar = false, isLoading = true)
            val result = recipeRepo.eliminarReceta(id)
            result.onSuccess {
                _state.value = _state.value.copy(
                    mensajeExito = "Receta eliminada con éxito"
                )
                cargarRecetas()
            }.onFailure { err ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error al eliminar la receta: ${err.message}"
                )
            }
        }
    }

    fun limpiarMensajes() {
        _state.value = _state.value.copy(errorMessage = null, mensajeExito = null)
    }

    private fun aplicarFiltros(
        lista: List<Receta>,
        query: String,
        categoria: String,
        dificultad: String,
        soloMisRecetas: Boolean,
        userId: String
    ): List<Receta> {
        return lista.filter { receta ->
            val coincideAutor = !soloMisRecetas || (userId.isNotBlank() && receta.autorId == userId)

            val coincideQuery = query.isBlank() ||
                    receta.titulo.contains(query, ignoreCase = true) ||
                    receta.descripcion.contains(query, ignoreCase = true) ||
                    receta.tags.any { tag -> tag.contains(query, ignoreCase = true) }

            val coincideCategoria = categoria.equals("Todas", ignoreCase = true) ||
                    receta.categoria.contains(categoria, ignoreCase = true) ||
                    receta.categoria.equals(categoria, ignoreCase = true)

            val coincideDificultad = dificultad.equals("Todas", ignoreCase = true) ||
                    receta.dificultad.equals(dificultad, ignoreCase = true) ||
                    (dificultad.equals("Medio", ignoreCase = true) && (receta.dificultad.equals("Media", ignoreCase = true) || receta.dificultad.equals("Intermedio", ignoreCase = true))) ||
                    (dificultad.equals("Fácil", ignoreCase = true) && receta.dificultad.equals("Principiante", ignoreCase = true))

            coincideAutor && coincideQuery && coincideCategoria && coincideDificultad
        }
    }
}
