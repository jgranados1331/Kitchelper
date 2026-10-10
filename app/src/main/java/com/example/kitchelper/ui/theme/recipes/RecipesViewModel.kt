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
                _state.value = _state.value.copy(
                    isLoading = false,
                    recetas = lista,
                    recetasFiltradas = lista
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
        val filtradas = current.recetas.filter {
            it.titulo.contains(query, ignoreCase = true) ||
            it.descripcion.contains(query, ignoreCase = true) ||
            it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
        }
        _state.value = current.copy(searchQuery = query, recetasFiltradas = filtradas)
    }

    fun filtrarPorCategoria(categoria: String) {
        val current = _state.value
        val filtradas = if (categoria == "Todas") {
            current.recetas
        } else {
            current.recetas.filter { it.categoria.equals(categoria, ignoreCase = true) }
        }
        _state.value = current.copy(categoriaSeleccionada = categoria, recetasFiltradas = filtradas)
    }
}
