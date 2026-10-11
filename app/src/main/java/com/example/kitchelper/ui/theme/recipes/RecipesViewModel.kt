package com.example.kitchelper.ui.theme.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kitchelper.data.model.Receta
import com.example.kitchelper.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipesState(
    val isLoading: Boolean = true,
    val recetas: List<Receta> = emptyList(),
    val recetasFiltradas: List<Receta> = emptyList(),
    val searchQuery: String = "",
    val categoriaSeleccionada: String = "Todas",
    val dificultadSeleccionada: String = "Todas",
    val errorMessage: String? = null
)

class RecipesViewModel(
    private val recipeRepo: RecipeRepository = RecipeRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(RecipesState())
    val state: StateFlow<RecipesState> = _state.asStateFlow()

    init {
        cargarRecetas()
    }

    fun cargarRecetas() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = recipeRepo.obtenerRecetas()
            result.onSuccess { lista ->
                val current = _state.value
                val filtradas = aplicarFiltros(lista, current.searchQuery, current.categoriaSeleccionada, current.dificultadSeleccionada)
                _state.value = current.copy(
                    isLoading = false,
                    recetas = lista,
                    recetasFiltradas = filtradas
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al cargar recetas"
                )
            }
        }
    }

    fun buscarRecetas(query: String) {
        val current = _state.value
        val filtradas = aplicarFiltros(current.recetas, query, current.categoriaSeleccionada, current.dificultadSeleccionada)
        _state.value = current.copy(searchQuery = query, recetasFiltradas = filtradas)
    }

    fun filtrarPorCategoria(categoria: String) {
        val current = _state.value
        val nuevaCat = if (current.categoriaSeleccionada.equals(categoria, ignoreCase = true)) "Todas" else categoria
        val filtradas = aplicarFiltros(current.recetas, current.searchQuery, nuevaCat, current.dificultadSeleccionada)
        _state.value = current.copy(categoriaSeleccionada = nuevaCat, recetasFiltradas = filtradas)
    }

    fun filtrarPorDificultad(dificultad: String) {
        val current = _state.value
        val nuevaDif = if (current.dificultadSeleccionada.equals(dificultad, ignoreCase = true)) "Todas" else dificultad
        val filtradas = aplicarFiltros(current.recetas, current.searchQuery, current.categoriaSeleccionada, nuevaDif)
        _state.value = current.copy(dificultadSeleccionada = nuevaDif, recetasFiltradas = filtradas)
    }

    private fun aplicarFiltros(
        lista: List<Receta>,
        query: String,
        categoria: String,
        dificultad: String
    ): List<Receta> {
        return lista.filter { receta ->
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

            coincideQuery && coincideCategoria && coincideDificultad
        }
    }
}
