package com.example.kitchelper.ar

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Estado UI de la pantalla de escaneo AR de ingredientes.
 */
data class ArIngredientUiState(
    val currentIngredient: IngredientAnalyzer.DetectedIngredient? = null,
    val savedIngredients: List<String> = emptyList(),
    val userFeedbackMessage: String? = null
)

/**
 * ViewModel encargado de la gestión del estado del escáner de ingredientes.
 *
 * Separa la lógica de presentación de la capa de visión por computador (ML Kit/ARCore).
 */
class ArIngredientViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ArIngredientUiState())
    val uiState: StateFlow<ArIngredientUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el ingrediente enfocado actualmente en pantalla a partir del análisis en tiempo real.
     */
    fun onIngredientDetected(ingredient: IngredientAnalyzer.DetectedIngredient?) {
        _uiState.update { currentState ->
            currentState.copy(currentIngredient = ingredient)
        }
    }

    /**
     * Registra/guarda el ingrediente que el usuario tiene enfocado actualmente.
     * Solo se ejecuta cuando el usuario presiona explícitamente el botón "Registrar Ingrediente".
     */
    fun registerCurrentIngredient() {
        val ingredientToRegister = _uiState.value.currentIngredient?.name ?: return

        _uiState.update { currentState ->
            val updatedList = if (!currentState.savedIngredients.contains(ingredientToRegister)) {
                currentState.savedIngredients + ingredientToRegister
            } else {
                currentState.savedIngredients
            }

            currentState.copy(
                savedIngredients = updatedList,
                userFeedbackMessage = "¡Ingrediente '$ingredientToRegister' registrado!"
            )
        }
    }

    /**
     * Elimina un ingrediente de la lista de ingredientes guardados.
     */
    fun removeSavedIngredient(ingredientName: String) {
        _uiState.update { currentState ->
            currentState.copy(
                savedIngredients = currentState.savedIngredients - ingredientName
            )
        }
    }

    /**
     * Limpia el mensaje de retroalimentación tras ser consumido por la UI (ej. Toast).
     */
    fun clearUserFeedback() {
        _uiState.update { currentState ->
            currentState.copy(userFeedbackMessage = null)
        }
    }

    /**
     * Limpia todos los ingredientes guardados para iniciar un nuevo escaneo.
     */
    fun clearAllSavedIngredients() {
        _uiState.update { currentState ->
            currentState.copy(savedIngredients = emptyList())
        }
    }
}
