package com.example.kitchelper.ui.theme.practices

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kitchelper.data.model.PracticaBasica
import com.example.kitchelper.ui.theme.FrauncesFamily
import com.example.kitchelper.ui.theme.InterFamily
import com.example.kitchelper.ui.theme.InriaSerifFamily
import com.example.kitchelper.ui.theme.components.BarraNavegacionInferior
import com.example.kitchelper.ui.theme.home.SelectorNivel
import com.example.kitchelper.ui.theme.navigation.Routes
import com.example.kitchelper.ui.theme.profile.CREMA
import com.example.kitchelper.ui.theme.profile.GRIS_TEXTO
import com.example.kitchelper.ui.theme.profile.VERDE_KITCHEN

@Composable
fun PracticesScreen(
    navController: NavController,
    viewModel: PracticesViewModel = viewModel()
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
                    Column {
                        Text(
                            "Básicos de Cocina",
                            fontFamily = InterFamily,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            "PRÁCTICAS BÁSICAS",
                            fontFamily = FrauncesFamily,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = VERDE_KITCHEN
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(2.dp, VERDE_KITCHEN),
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { navController.navigate(Routes.PROFILE) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "E",
                                fontFamily = FrauncesFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = VERDE_KITCHEN
                            )
                        }
                    }
                }
            }

            // ===== 2. BARRA DE BÚSQUEDA Y BOTÓN CREAR =====
            item(span = { GridItemSpan(2) }) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.buscarPracticas(it) },
                            placeholder = { Text("Buscar técnica o práctica...", fontFamily = InterFamily, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = VERDE_KITCHEN,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { navController.navigate(Routes.CREATE_RECIPE) },
                            colors = ButtonDefaults.buttonColors(containerColor = VERDE_KITCHEN),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)
                        ) {
                            Text("+ Crear", fontFamily = InterFamily, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // ===== 3. FILTRO DE EXPERTICIA =====
            item(span = { GridItemSpan(2) }) {
                Column {
                    Text(
                        "Dificultad",
                        fontFamily = InriaSerifFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GRIS_TEXTO
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SelectorNivel(
                        nivelUsuario = "Principiante",
                        nivelSeleccionado = state.dificultadSeleccionada,
                        onNivelChange = { viewModel.filtrarPorDificultad(it) }
                    )
                }
            }

            // ===== 4. SECCIÓN TODAS LAS PRÁCTICAS =====
            item(span = { GridItemSpan(2) }) {
                Text(
                    "Todas las prácticas",
                    fontFamily = InriaSerifFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GRIS_TEXTO
                )
            }

            // Grid de Prácticas
            if (state.isLoading) {
                item(span = { GridItemSpan(2) }) {
                    Box(modifier = Modifier.height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VERDE_KITCHEN)
                    }
                }
            } else if (state.practicasFiltradas.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(modifier = Modifier.height(150.dp), contentAlignment = Alignment.Center) {
                        Text("No se encontraron prácticas técnicas disponibles.", fontFamily = InterFamily, color = Color.Gray)
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
            rutaActual = "recipes",
            onInicioClick = { navController.navigate(Routes.HOME) },
            onRecetasClick = { navController.navigate(Routes.RECIPES) },
            onGuardadosClick = { navController.navigate(Routes.FAVORITOS) },
            onPerfilClick = { navController.navigate(Routes.PROFILE) },
            onFlechaClick = { navController.navigate(Routes.CREATE_RECIPE) }
        )
    }
}

// ============ TARJETA DE PRÁCTICA GRID ============
@Composable
fun TarjetaPracticaGrid(practica: PracticaBasica, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                if (practica.imagenUrl.isNotBlank()) {
                    AsyncImage(
                        model = practica.imagenUrl,
                        contentDescription = practica.titulo,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    practica.titulo.ifBlank { "Práctica Básica" },
                    fontFamily = InriaSerifFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GRIS_TEXTO,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    practica.descripcion,
                    fontFamily = InterFamily,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEBEBEB)
                    ) {
                        Text(
                            "${practica.tiempoMinutos} min",
                            fontFamily = InterFamily,
                            fontSize = 9.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (practica.ar?.tieneAR == true) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF2E6E6)
                        ) {
                            Text(
                                "VR",
                                fontFamily = InterFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6B4A),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE8F0D6)
                    ) {
                        Text(
                            practica.dificultad.ifBlank { "Principiante" },
                            fontFamily = InterFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = VERDE_KITCHEN,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
