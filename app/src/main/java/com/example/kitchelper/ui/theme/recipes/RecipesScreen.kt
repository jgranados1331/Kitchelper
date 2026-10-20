package com.example.kitchelper.ui.theme.recipes

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kitchelper.R
import com.example.kitchelper.data.model.Receta
import com.example.kitchelper.ui.theme.FrauncesFamily
import com.example.kitchelper.ui.theme.InterFamily
import com.example.kitchelper.ui.theme.InriaSerifFamily
import com.example.kitchelper.ui.theme.components.BarraNavegacionInferior
import com.example.kitchelper.ui.theme.home.SelectorNivel
import com.example.kitchelper.ui.theme.navigation.Routes

@Composable
fun RecipesScreen(
    navController: NavController,
    viewModel: RecipesViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.mensajeExito) {
        state.mensajeExito?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajes()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limpiarMensajes()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF6EC))
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // ===== 1. HEADER KITCHELPER =====
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Descubre y Crea",
                            fontFamily = InterFamily,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            "RECETAS",
                            fontFamily = FrauncesFamily,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A6B4A)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(2.dp, Color(0xFF4A6B4A)),
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
                                color = Color(0xFF4A6B4A)
                            )
                        }
                    }
                }
            }

            // ===== 2. SECCIÓN CATEGORÍAS CON IMÁGENES LOCALES =====
            item(span = { GridItemSpan(2) }) {
                Column {
                    Text(
                        "Categorías",
                        fontFamily = InriaSerifFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A4A4A)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ItemCategoriaCard(
                                titulo = "Desayunos",
                                drawableResId = R.drawable.desayuno,
                                estaSeleccionada = state.categoriaSeleccionada.equals("Desayuno", ignoreCase = true),
                                onClick = { viewModel.filtrarPorCategoria("Desayuno") }
                            )
                        }
                        item {
                            ItemCategoriaCard(
                                titulo = "Almuerzo",
                                drawableResId = R.drawable.almuerzo,
                                estaSeleccionada = state.categoriaSeleccionada.equals("Almuerzo", ignoreCase = true),
                                onClick = { viewModel.filtrarPorCategoria("Almuerzo") }
                            )
                        }
                        item {
                            ItemCategoriaCard(
                                titulo = "Cena",
                                drawableResId = R.drawable.comida,
                                estaSeleccionada = state.categoriaSeleccionada.equals("Cena",ignoreCase = true),
                                onClick = { viewModel.filtrarPorCategoria("Cena") }
                            )
                        }
                    }
                }
            }

            // ===== 3. SECCIÓN EXPERTICIA (FILTROS) =====
            item(span = { GridItemSpan(2) }) {
                Column {
                    Text(
                        "Experticia",
                        fontFamily = InriaSerifFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A4A4A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SelectorNivel(
                        nivelUsuario = "Principiante",
                        nivelSeleccionado = state.dificultadSeleccionada,
                        onNivelChange = { viewModel.filtrarPorDificultad(it) }
                    )
                }
            }

            // ===== 4. SECCIÓN RECETAS CON PESTAÑA MIS CREACIONES =====
            item(span = { GridItemSpan(2) }) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (state.soloMisRecetas) "Mis Recetas Creadas" else "Todas las recetas",
                            fontFamily = InriaSerifFamily,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A4A4A)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // BOTON DE ESCANER IA
                            Button(
                                onClick = {
                                    navController.navigate(Routes.AR_SCAN)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A6B4A)),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "✦ Escáner IA",
                                    fontFamily = InterFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // A CONTNUACION ES EL BOTON DE CREAR RECETA

                            Button(
                                onClick = { navController.navigate(Routes.CREATE_RECIPE) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A6B4A)),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "+ Crear Receta",
                                    fontFamily = InterFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botones de filtro: "Todas las Recetas" vs "Mis Recetas (Creadas por mí)"
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = !state.soloMisRecetas,
                            onClick = { viewModel.toggleSoloMisRecetas(false) },
                            label = { Text("Todas", fontFamily = InterFamily, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF4A6B4A),
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = state.soloMisRecetas,
                            onClick = { viewModel.toggleSoloMisRecetas(true) },
                            label = { Text("Mis Creaciones", fontFamily = InterFamily, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE07A5F),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Grid de Recetas (2 columnas)
            if (state.isLoading) {
                item(span = { GridItemSpan(2) }) {
                    Box(modifier = Modifier.height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF4A6B4A))
                    }
                }
            } else if (state.recetasFiltradas.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(modifier = Modifier.height(150.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (state.soloMisRecetas) "Aún no has creado recetas. ¡Usa el botón '+ Crear Receta' para empezar!" else "No se encontraron recetas con los filtros seleccionados",
                            fontFamily = InterFamily,
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(state.recetasFiltradas) { receta ->
                    val esMio = state.currentUserId.isNotBlank() && receta.autorId == state.currentUserId
                    val esFavorito = state.favoritoIds.contains(receta.id)

                    TarjetaRecetaGrid(
                        receta = receta,
                        esRecetaPropia = esMio,
                        esFavorito = esFavorito,
                        onFavoritoClick = { viewModel.toggleFavorito(receta.id) },
                        onEditClick = { navController.navigate("create_recipe?recetaId=${receta.id}") },
                        onDeleteClick = { viewModel.solicitarEliminarReceta(receta.id) },
                        onClick = { navController.navigate("recipe_detail/${receta.id}") }
                    )
                }
            }
        }

        // Alerta de confirmación para eliminar receta creada por el usuario
        if (state.showDialogConfirmarEliminar) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelarEliminarReceta() },
                title = {
                    Text(
                        "Eliminar Receta",
                        fontFamily = FrauncesFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935)
                    )
                },
                text = {
                    Text(
                        "¿Estás seguro de que deseas eliminar esta receta? Se borrará permanentemente de la plataforma.",
                        fontFamily = InterFamily,
                        fontSize = 14.sp,
                        color = Color(0xFF4A4A4A)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmarEliminarReceta() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Text("Eliminar", fontFamily = InterFamily, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { viewModel.cancelarEliminarReceta() }) {
                        Text("Cancelar", fontFamily = InterFamily, color = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }

        // Barra de navegación inferior flotante
        BarraNavegacionInferior(
            modifier = Modifier.align(Alignment.BottomCenter),
            rutaActual = "recipes",
            onInicioClick = { navController.navigate(Routes.HOME) },
            onRecetasClick = { /* Ya estamos en Recetas */ },
            onGuardadosClick = { navController.navigate(Routes.FAVORITOS) },
            onPerfilClick = { navController.navigate(Routes.PROFILE) },
            onFlechaClick = { navController.navigate(Routes.CREATE_RECIPE) }
        )
    }
}

// ============ TARJETA DE CATEGORÍA CON IMAGEN LOCAL ============
@Composable
fun ItemCategoriaCard(
    titulo: String,
    drawableResId: Int,
    estaSeleccionada: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            shadowElevation = if (estaSeleccionada) 4.dp else 1.dp,
            border = if (estaSeleccionada) BorderStroke(2.dp, Color(0xFF4A6B4A)) else null,
            modifier = Modifier.size(90.dp)
        ) {
            Image(
                painter = painterResource(id = drawableResId),
                contentDescription = titulo,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            titulo,
            fontFamily = InriaSerifFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (estaSeleccionada) Color(0xFF4A6B4A) else Color(0xFF4A4A4A)
        )
    }
}

// ============ TARJETA DE RECETA GRID 2 COLUMNAS ============
@Composable
fun TarjetaRecetaGrid(
    receta: Receta,
    esRecetaPropia: Boolean = false,
    esFavorito: Boolean = false,
    onFavoritoClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onClick: () -> Unit
) {
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

                // Ícono de Estrella Favorito
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.35f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clickable { onFavoritoClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (esFavorito) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorito",
                            tint = if (esFavorito) Color(0xFFFFC107) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Si la receta fue creada por el usuario actual, mostrar insignias Editar y Eliminar
                if (esRecetaPropia) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4A6B4A),
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onEditClick() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar mi receta",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE53935),
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onDeleteClick() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar mi receta",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    receta.titulo.ifBlank { "Receta sin título" },
                    fontFamily = InriaSerifFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF4A4A4A),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Píldora Tiempo
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

                    // Píldora VR / AR
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

                    // Píldora Categoría
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
