package com.example.kitchelper.ui.theme.practices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kitchelper.data.model.PracticaBasica
import com.example.kitchelper.data.repository.PracticeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PracticesState(
    val isLoading: Boolean = true,
    val practicas: List<PracticaBasica> = emptyList(),
    val practicasFiltradas: List<PracticaBasica> = emptyList(),
    val searchQuery: String = "",
    val dificultadSeleccionada: String = "Todas",
    val errorMessage: String? = null
)

class PracticesViewModel(
    private val practiceRepo: PracticeRepository = PracticeRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(PracticesState())
    val state: StateFlow<PracticesState> = _state.asStateFlow()

    init {
        cargarPracticas()
    }

    fun cargarPracticas() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = practiceRepo.obtenerPracticas()
            result.onSuccess { lista ->
                val current = _state.value
                val filtradas = aplicarFiltros(lista, current.searchQuery, current.dificultadSeleccionada)
                _state.value = current.copy(
                    isLoading = false,
                    practicas = lista,
                    practicasFiltradas = filtradas
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al cargar prácticas"
                )
            }
        }
    }

    fun buscarPracticas(query: String) {
        val current = _state.value
        val filtradas = aplicarFiltros(current.practicas, query, current.dificultadSeleccionada)
        _state.value = current.copy(searchQuery = query, practicasFiltradas = filtradas)
    }

    fun filtrarPorDificultad(dificultad: String) {
        val current = _state.value
        val nuevaDif = if (current.dificultadSeleccionada.equals(dificultad, ignoreCase = true)) "Todas" else dificultad
        val filtradas = aplicarFiltros(current.practicas, current.searchQuery, nuevaDif)
        _state.value = current.copy(dificultadSeleccionada = nuevaDif, practicasFiltradas = filtradas)
    }

    private fun aplicarFiltros(
        lista: List<PracticaBasica>,
        query: String,
        dificultad: String
    ): List<PracticaBasica> {
        return lista.filter { practica ->
            val coincideQuery = query.isBlank() ||
                    practica.titulo.contains(query, ignoreCase = true) ||
                    practica.descripcion.contains(query, ignoreCase = true)

            val coincideDificultad = dificultad.equals("Todas", ignoreCase = true) ||
                    practica.dificultad.equals(dificultad, ignoreCase = true) ||
                    (dificultad.equals("Medio", ignoreCase = true) && (practica.dificultad.equals("Media", ignoreCase = true) || practica.dificultad.equals("Intermedio", ignoreCase = true))) ||
                    (dificultad.equals("Fácil", ignoreCase = true) && practica.dificultad.equals("Principiante", ignoreCase = true))

            coincideQuery && coincideDificultad
        }
    }
}
