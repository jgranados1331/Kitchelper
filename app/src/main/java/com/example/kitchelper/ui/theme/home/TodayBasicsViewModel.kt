package com.example.kitchelper.ui.theme.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kitchelper.data.model.PracticaBasica
import com.example.kitchelper.data.repository.PracticeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TodayBasicsState(
    val isLoading: Boolean = true,
    val practicasHoy: List<PracticaBasica> = emptyList(),
    val practicasFiltradas: List<PracticaBasica> = emptyList(),
    val searchQuery: String = "",
    val dificultadSeleccionada: String = "Todas",
    val errorMessage: String? = null
)

class TodayBasicsViewModel(
    private val practiceRepo: PracticeRepository = PracticeRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(TodayBasicsState())
    val state: StateFlow<TodayBasicsState> = _state.asStateFlow()

    init {
        cargarBasicosDeHoy()
    }

    fun cargarBasicosDeHoy() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = practiceRepo.obtenerPracticas()
            result.onSuccess { lista ->
                // Rotación determinista cada 24 horas usando la fecha del día como semilla
                val seedDia = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
                val seleccion24h = if (lista.isNotEmpty()) {
                    lista.shuffled(java.util.Random(seedDia))
                } else emptyList()

                val current = _state.value
                val filtradas = aplicarFiltros(seleccion24h, current.searchQuery, current.dificultadSeleccionada)
                _state.value = current.copy(
                    isLoading = false,
                    practicasHoy = seleccion24h,
                    practicasFiltradas = filtradas
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al cargar los básicos de hoy"
                )
            }
        }
    }

    fun buscarBasicos(query: String) {
        val current = _state.value
        val filtradas = aplicarFiltros(current.practicasHoy, query, current.dificultadSeleccionada)
        _state.value = current.copy(searchQuery = query, practicasFiltradas = filtradas)
    }

    fun filtrarPorDificultad(dificultad: String) {
        val current = _state.value
        val nuevaDif = if (current.dificultadSeleccionada.equals(dificultad, ignoreCase = true)) "Todas" else dificultad
        val filtradas = aplicarFiltros(current.practicasHoy, current.searchQuery, nuevaDif)
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
