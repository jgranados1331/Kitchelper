package com.example.kitchelper.ui.theme.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.example.kitchelper.data.model.Receta
import com.example.kitchelper.data.repository.FavoriteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FavoritesState(
    val isLoading: Boolean = true,
    val recetasFavoritas: List<Receta> = emptyList(),
    val favoritoIds: Set<String> = emptySet(),
    val errorMessage: String? = null
)

class FavoritesViewModel(
    private val favoriteRepo: FavoriteRepository = FavoriteRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = _state.asStateFlow()

    init {
        cargarFavoritos()
    }

    fun cargarFavoritos() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val result = favoriteRepo.obtenerRecetasFavoritas(userId)
            val idsResult = favoriteRepo.obtenerIdsFavoritos(userId)

            result.onSuccess { lista ->
                val ids = idsResult.getOrNull() ?: lista.map { it.id }.toSet()
                _state.value = FavoritesState(
                    isLoading = false,
                    recetasFavoritas = lista,
                    favoritoIds = ids
                )
            }.onFailure { err ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Error al cargar favoritos"
                )
            }
        }
    }

    fun toggleFavorito(recetaId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            val result = favoriteRepo.toggleFavorito(userId, recetaId)
            result.onSuccess { fueAgregado ->
                if (fueAgregado) {
                    val nuevosIds = _state.value.favoritoIds + recetaId
                    _state.value = _state.value.copy(favoritoIds = nuevosIds)
                } else {
                    val nuevosIds = _state.value.favoritoIds - recetaId
                    val listaFiltrada = _state.value.recetasFavoritas.filter { it.id != recetaId }
                    _state.value = _state.value.copy(
                        favoritoIds = nuevosIds,
                        recetasFavoritas = listaFiltrada
                    )
                }
            }
        }
    }
}
