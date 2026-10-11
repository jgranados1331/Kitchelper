package com.example.kitchelper.ui.theme.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.example.kitchelper.data.model.PracticaBasica
import com.example.kitchelper.data.model.Receta
import com.example.kitchelper.data.model.Usuario
import com.example.kitchelper.data.repository.PracticeRepository
import com.example.kitchelper.data.repository.RecipeRepository
import com.example.kitchelper.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeState(
    val isLoading: Boolean = true,
    val usuario: Usuario? = null,
    val recetaDestacada: Receta? = null,
    val recetasDelNivel: List<Receta> = emptyList(),
    val practicasBasicas: List<PracticaBasica> = emptyList(),
    val nivelSeleccionado: String = "Principiante",
    val errorMessage: String? = null
)

class HomeViewModel(
    private val userRepo: UserRepository = UserRepository(),
    private val recipeRepo: RecipeRepository = RecipeRepository(),
    private val practiceRepo: PracticeRepository = PracticeRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        cargarHome()
    }

    fun cargarHome() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // 1. Cargar usuario
            val usuarioResult = userRepo.obtenerUsuario(userId)
            val usuario = usuarioResult.getOrNull()

            // 2. Cargar recetas
            val recetasResult = recipeRepo.obtenerRecetas()
            val recetas = recetasResult.getOrNull() ?: emptyList()

            // 3. Cargar prácticas
            val practicasResult = practiceRepo.obtenerPracticas()
            val practicas = practicasResult.getOrNull() ?: emptyList()

            // 4. Seleccionar receta destacada (aleatoria o personalizada)
            val recetaDestacada = seleccionarRecetaDestacada(recetas, usuario)

            // 5. Filtrar recetas por nivel del usuario (con fallback a cualquier receta si no hay o está vacío)
            val nivel = usuario?.informacionCulinaria?.experticia?.takeIf { it.isNotBlank() } ?: "Principiante"
            var recetasDelNivel = recetas.filter { receta ->
                receta.dificultad.equals(nivel, ignoreCase = true) ||
                (nivel.equals("Intermedio", ignoreCase = true) && (receta.dificultad.equals("Fácil", ignoreCase = true) || receta.dificultad.equals("Media", ignoreCase = true)))
            }.take(2)
            if (recetasDelNivel.isEmpty()) {
                recetasDelNivel = recetas.take(2)
            }

            _state.value = HomeState(
                isLoading = false,
                usuario = usuario,
                recetaDestacada = recetaDestacada,
                recetasDelNivel = recetasDelNivel,
                practicasBasicas = practicas.take(2),
                nivelSeleccionado = nivel
            )
        }
    }

    fun cambiarNivel(nivel: String) {
        viewModelScope.launch {
            val todasResult = recipeRepo.obtenerRecetas()
            val todas = todasResult.getOrNull() ?: emptyList()
            var recetas = todas.filter { receta ->
                receta.dificultad.equals(nivel, ignoreCase = true) ||
                (nivel.equals("Intermedio", ignoreCase = true) && (receta.dificultad.equals("Fácil", ignoreCase = true) || receta.dificultad.equals("Media", ignoreCase = true)))
            }
            if (recetas.isEmpty()) {
                recetas = todas
            }
            _state.value = _state.value.copy(
                nivelSeleccionado = nivel,
                recetasDelNivel = recetas.take(2)
            )
        }
    }

    /**
     * Lógica de IA simplificada (Content-Based Filtering):
     * - Si perfil está incompleto → receta aleatoria
     * - Si perfil está completo → receta con mayor puntaje según preferencias
     */
    private fun seleccionarRecetaDestacada(recetas: List<Receta>, usuario: Usuario?): Receta? {
        if (recetas.isEmpty()) return null

        // Si el perfil está incompleto o no hay usuario, devolver aleatoria
        if (usuario == null || !usuario.perfilCompleto) {
            return recetas.random()
        }

        // Filtrar recetas que no tengan alérgenos del usuario
        val recetasSeguras = recetas.filter { receta ->
            receta.ingredientes.none { ingrediente ->
                usuario.informacionSalud.alergias.any { alergia ->
                    ingrediente.nombre.contains(alergia, ignoreCase = true)
                }
            }
        }

        if (recetasSeguras.isEmpty()) return recetas.random()

        // Calcular puntaje y ordenar
        val recetaConPuntaje = recetasSeguras.map { receta ->
            receta to calcularPuntaje(receta, usuario)
        }.sortedByDescending { it.second }

        return recetaConPuntaje.firstOrNull()?.first
    }

    private fun calcularPuntaje(receta: Receta, usuario: Usuario): Int {
        var puntaje = 0

        // Nivel de experticia
        if (receta.dificultad.equals(usuario.informacionCulinaria.experticia, ignoreCase = true)) puntaje += 10
        if (receta.dificultad.equals("Fácil", ignoreCase = true) && usuario.informacionCulinaria.experticia.equals("Principiante", ignoreCase = true)) puntaje += 5

        // Preferencias de comida
        if (receta.tags.any { tag ->
                tag.contains(usuario.informacionCulinaria.preferenciaComida, ignoreCase = true) ||
                tag.contains(usuario.informacionCulinaria.tipoComida, ignoreCase = true)
            }) puntaje += 5

        // Tipo de alimentación
        if (usuario.informacionSalud.tipoAlimentacion.equals("Fitness", ignoreCase = true) &&
            receta.tags.contains("saludable")) puntaje += 3

        return puntaje
    }

    fun limpiarError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
