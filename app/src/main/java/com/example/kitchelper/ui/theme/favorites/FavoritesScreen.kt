package com.example.kitchelper.ui.theme.favorites

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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kitchelper.data.model.Receta
import com.example.kitchelper.ui.theme.FrauncesFamily
import com.example.kitchelper.ui.theme.InterFamily
import com.example.kitchelper.ui.theme.InriaSerifFamily
import com.example.kitchelper.ui.theme.components.BarraNavegacionInferior
import com.example.kitchelper.ui.theme.navigation.Routes
import com.example.kitchelper.ui.theme.profile.CREMA
import com.example.kitchelper.ui.theme.profile.GRIS_TEXTO
import com.example.kitchelper.ui.theme.profile.VERDE_KITCHEN

val AMARILLO_ESTRELLA = Color(0xFFFFC107)

@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoritesViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Recargar favoritos al abrir la pantalla
    LaunchedEffect(Unit) {
        viewModel.cargarFavoritos()
    }

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
                            "Guardados",
                            fontFamily = InterFamily,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            "MIS FAVORITOS",
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

            // ===== 2. CONTENIDO =====
            if (state.isLoading) {
                item(span = { GridItemSpan(2) }) {
                    Box(modifier = Modifier.height(250.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VERDE_KITCHEN)
                    }
                }
            } else if (state.recetasFavoritas.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = AMARILLO_ESTRELLA,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Aún no tienes recetas favoritas",
                                fontFamily = InriaSerifFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GRIS_TEXTO
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Presiona la estrella en cualquier receta para guardarla aquí y acceder rápidamente.",
                                fontFamily = InterFamily,
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { navController.navigate(Routes.RECIPES) },
                                colors = ButtonDefaults.buttonColors(containerColor = VERDE_KITCHEN),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Explorar Recetas", fontFamily = InterFamily, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(state.recetasFavoritas) { receta ->
                    TarjetaFavoritaItem(
                        receta = receta,
                        esFavorito = state.favoritoIds.contains(receta.id),
                        onStarClick = { viewModel.toggleFavorito(receta.id) },
                        onCardClick = { navController.navigate("recipe_detail/${receta.id}") }
                    )
                }
            }
        }

        // Barra de navegación inferior flotante
        BarraNavegacionInferior(
            modifier = Modifier.align(Alignment.BottomCenter),
            rutaActual = "favoritos",
            onInicioClick = { navController.navigate(Routes.HOME) },
            onRecetasClick = { navController.navigate(Routes.RECIPES) },
            onGuardadosClick = { /* Ya estamos en Favoritos */ },
            onPerfilClick = { navController.navigate(Routes.PROFILE) },
            onFlechaClick = { navController.navigate(Routes.CREATE_RECIPE) }
        )
    }
}

@Composable
fun TarjetaFavoritaItem(
    receta: Receta,
    esFavorito: Boolean,
    onStarClick: () -> Unit,
    onCardClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                if (receta.imagenUrl.isNotBlank()) {
                    AsyncImage(
                        model = receta.imagenUrl,
                        contentDescription = receta.titulo,
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

                // Ícono de Estrella (Amarillo si es favorito)
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.35f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clickable { onStarClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Favorito",
                            tint = if (esFavorito) AMARILLO_ESTRELLA else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    receta.titulo.ifBlank { "Receta Favorita" },
                    fontFamily = InriaSerifFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GRIS_TEXTO,
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
                            "${receta.tiempoMinutos} min",
                            fontFamily = InterFamily,
                            fontSize = 9.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

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

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEBEBEB)
                    ) {
                        Text(
                            receta.categoria.ifBlank { "Desayuno" },
                            fontFamily = InterFamily,
                            fontSize = 9.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
