package com.example.kitchelper.ui.theme.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kitchelper.data.model.PracticaBasica
import com.example.kitchelper.data.model.Receta
import com.example.kitchelper.ui.theme.navigation.Routes

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var menuExpandido by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF6EC))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 1. Header con avatar y menú desplegable
            item {
                HeaderHome(
                    nombreUsuario = state.usuario?.nombre ?: "Usuario",
                    menuExpandido = menuExpandido,
                    onToggleMenu = { menuExpandido = !menuExpandido },
                    onPerfilClick = { navController.navigate(Routes.PROFILE) },
                    onConfiguracionClick = { navController.navigate(Routes.PROFILE) },
                    onCerrarSesionClick = {
                        // TODO: Implementar logout
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // 2. Aviso de completar perfil (si aplica)
            if (state.usuario?.perfilCompleto == false) {
                item {
                    AvisoCompletarPerfil(
                        onCompletarClick = { navController.navigate(Routes.PROFILE) }
                    )
                }
            }

            // 3. Receta destacada
            state.recetaDestacada?.let { receta ->
                item {
                    RecetaDestacada(receta = receta, onEmpezar = {
                        navController.navigate("recipe_detail/${receta.id}")
                    })
                }
            }

            // 4. Guía interactiva AR
            item {
                GuiaInteractivaAR(
                    onCalibrarClick = { navController.navigate(Routes.AR_GUIDE) },
                    onVistaARClick = { navController.navigate(Routes.AR_GUIDE) }
                )
            }

            // 5. Selector de nivel
            item {
                SelectorNivel(
                    nivelSeleccionado = state.nivelSeleccionado,
                    onNivelChange = { viewModel.cambiarNivel(it) }
                )
            }

            // 6. Recetas del día
            item {
                Text(
                    "Recetas de hoy",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.recetasDelNivel) { receta ->
                        TarjetaReceta(receta = receta, onClick = {
                            navController.navigate("recipe_detail/${receta.id}")
                        })
                    }
                }
            }

            // 7. Prácticas básicas
            item {
                Text(
                    "Básicos de hoy",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(state.practicasBasicas) { practica ->
                TarjetaPractica(practica = practica, onClick = {
                    navController.navigate("practice_detail/${practica.id}")
                })
            }
        }

        // Barra de navegación inferior flotante
        BarraNavegacionInferior(
            modifier = Modifier.align(Alignment.BottomCenter),
            onInicioClick = { /* Ya estamos en Home */ },
            onRecetasClick = { navController.navigate(Routes.RECIPES) },
            onGuardadosClick = { navController.navigate(Routes.FAVORITOS) },
            onPerfilClick = { navController.navigate(Routes.PROFILE) },
            onFlechaClick = { viewModel.cargarHome() }
        )
    }
}