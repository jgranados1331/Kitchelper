package com.example.kitchelper.ui.theme.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import com.example.kitchelper.ui.theme.InriaSerifFamily
import com.example.kitchelper.ui.theme.components.BarraNavegacionInferior
import com.example.kitchelper.ui.theme.navigation.Routes
import com.example.kitchelper.ui.theme.practices.TarjetaPracticaGrid
import com.example.kitchelper.ui.theme.profile.CREMA
import com.example.kitchelper.ui.theme.profile.GRIS_TEXTO
import com.example.kitchelper.ui.theme.profile.VERDE_KITCHEN

@Composable
fun TodayBasicsScreen(
    navController: NavController,
    viewModel: TodayBasicsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CREMA)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // ===== 1. HEADER PANTALLA =====
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { navController.popBackStack() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Atrás",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Módulo Diario (Rotación 24h)",
                                fontFamily = InterFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "BÁSICOS DE HOY",
                                fontFamily = FrauncesFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { navController.navigate(Routes.PROFILE) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "E",
                                fontFamily = FrauncesFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // ===== 2. BARRA DE BÚSQUEDA =====
            item(span = { GridItemSpan(2) }) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.buscarBasicos(it) },
                    placeholder = { Text("Buscar en los básicos de hoy...", fontFamily = InterFamily, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ===== 3. FILTRO DE EXPERTICIA / DIFICULTAD =====
            item(span = { GridItemSpan(2) }) {
                Column {
                    Text(
                        "Dificultad",
                        fontFamily = InriaSerifFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SelectorNivel(
                        nivelUsuario = "Principiante",
                        nivelSeleccionado = state.dificultadSeleccionada,
                        onNivelChange = { viewModel.filtrarPorDificultad(it) }
                    )
                }
            }

            // ===== 4. LISTA DE BÁSICOS DE HOY =====
            item(span = { GridItemSpan(2) }) {
                Text(
                    "Técnicas recomendadas para hoy",
                    fontFamily = InriaSerifFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GRIS_TEXTO
                )
            }

            if (state.isLoading) {
                item(span = { GridItemSpan(2) }) {
                    Box(modifier = Modifier.height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (state.practicasFiltradas.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(modifier = Modifier.height(150.dp), contentAlignment = Alignment.Center) {
                        Text("No se encontraron básicos de hoy con los filtros seleccionados.", fontFamily = InterFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(state.practicasFiltradas) { practica ->
                    TarjetaPracticaGrid(
                        practica = practica,
                        onClick = { navController.navigate("practice_detail/${practica.id}") }
                    )
                }
            }
        }

        // Barra de navegación inferior flotante
        BarraNavegacionInferior(
            modifier = Modifier.align(Alignment.BottomCenter),
            rutaActual = "home",
            onInicioClick = { navController.navigate(Routes.HOME) },
            onRecetasClick = { navController.navigate(Routes.RECIPES) },
            onGuardadosClick = { navController.navigate(Routes.FAVORITOS) },
            onPerfilClick = { navController.navigate(Routes.PROFILE) },
            onFlechaClick = { navController.navigate(Routes.CREATE_RECIPE) }
        )
    }
}
