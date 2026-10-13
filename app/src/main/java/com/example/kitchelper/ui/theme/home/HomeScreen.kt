package com.example.kitchelper.ui.theme.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kitchelper.ui.theme.FrauncesFamily
import com.example.kitchelper.ui.theme.InterFamily
import com.example.kitchelper.ui.theme.auth.AuthLoadingDialog
import com.example.kitchelper.ui.theme.components.BarraNavegacionInferior
import com.example.kitchelper.ui.theme.navigation.Routes

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var menuExpandido by remember { mutableStateOf(false) }

    if (state.isLoading) {
        AuthLoadingDialog(message = "Entrando a la cocina y preparando tus recetas...")
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFDF6EC))
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // 1. Header con avatar, nivel de Firestore y menú desplegable
            item {
                HeaderHome(
                    nombreUsuario = state.usuario?.nombre ?: "Usuario",
                    nivelUsuario = state.usuario?.informacionCulinaria?.experticia ?: "Principiante",
                    menuExpandido = menuExpandido,
                    onToggleMenu = { menuExpandido = !menuExpandido },
                    onPerfilClick = { navController.navigate(Routes.PROFILE) },
                    onPuntosClick = { navController.navigate(Routes.PROFILE) },
                    onConfiguracionClick = { navController.navigate(Routes.PROFILE) },
                    onCerrarSesionClick = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // 2. Bloque 1: Aviso de completar perfil (si aplica)
            if (state.usuario?.perfilCompleto == false) {
                item {
                    AvisoCompletarPerfil(
                        onCompletarClick = { navController.navigate(Routes.PROFILE) }
                    )
                }
            }

            // 3. Bloque 2: Receta destacada aleatoria (con título en Inria Serif)
            state.recetaDestacada?.let { receta ->
                item {
                    RecetaDestacada(
                        receta = receta,
                        onEmpezar = { navController.navigate("recipe_detail/${receta.id}") }
                    )
                }
            }

            // 4. Bloque 3: Guía interactiva en VR y Selector de Nivel
            item {
                GuiaInteractivaAR(
                    onCalibrarClick = { navController.navigate(Routes.AR_GUIDE) },
                    onVistaARClick = { navController.navigate(Routes.AR_GUIDE) }
                )
            }

            item {
                SelectorNivel(
                    nivelUsuario = state.usuario?.informacionCulinaria?.experticia ?: "Principiante",
                    nivelSeleccionado = state.nivelSeleccionado,
                    onNivelChange = { viewModel.cambiarNivel(it) }
                )
            }

            // 5. Bloque 4: Recetas de hoy con configuración de dificultad (fallback a cualquier receta si no hay ninguna del nivel)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val nivelUsuario = state.usuario?.informacionCulinaria?.experticia?.takeIf { it.isNotBlank() } ?: "Principiante"
                    Text(
                        "Recetas de hoy - $nivelUsuario",
                        fontFamily = FrauncesFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A4A4A)
                    )
                    TextButton(onClick = { navController.navigate(Routes.RECIPES) }) {
                        Text(
                            "VER TODO",
                            fontFamily = InterFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B4A)
                        )
                    }
                }
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.recetasDelNivel) { receta ->
                        TarjetaReceta(
                            receta = receta,
                            onClick = { navController.navigate("recipe_detail/${receta.id}") }
                        )
                    }
                }
            }

            // 6. Bloque 5: Básicos de hoy (tomando básicos aleatorios del repositorio de prácticas básicas)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Básicos de hoy",
                        fontFamily = FrauncesFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A4A4A)
                    )
                    TextButton(onClick = { navController.navigate(Routes.PRACTICES) }) {
                        Text(
                            "VER TODO",
                            fontFamily = InterFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B4A)
                        )
                    }
                }
            }
            items(state.practicasBasicas) { practica ->
                TarjetaPractica(
                    practica = practica,
                    onClick = { navController.navigate("practice_detail/${practica.id}") }
                )
            }
        }

        // Barra de navegación inferior flotante

         BarraNavegacionInferior(
            modifier = Modifier.align(Alignment.BottomCenter),
            rutaActual = "home",
            onInicioClick = { /* Ya estamos en Home */ },
            onRecetasClick = { navController.navigate(Routes.RECIPES) },
            onGuardadosClick = { navController.navigate(Routes.FAVORITOS) },
            onPerfilClick = { navController.navigate(Routes.PROFILE) },
            onFlechaClick = { navController.navigate(Routes.CREATE_RECIPE) }
        )
    }
}
}
