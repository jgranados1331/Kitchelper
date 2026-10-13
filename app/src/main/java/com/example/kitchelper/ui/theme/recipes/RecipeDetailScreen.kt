package com.example.kitchelper.ui.theme.recipes

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.kitchelper.data.model.Ingrediente
import com.example.kitchelper.data.model.Paso
import com.example.kitchelper.data.model.Receta
import com.example.kitchelper.data.repository.FavoriteRepository
import com.example.kitchelper.ui.theme.FrauncesFamily
import com.example.kitchelper.ui.theme.InterFamily
import com.example.kitchelper.ui.theme.InriaSerifFamily
import com.example.kitchelper.ui.theme.components.BarraNavegacionInferior
import com.example.kitchelper.ui.theme.navigation.Routes
import com.example.kitchelper.ui.theme.profile.CREMA
import com.example.kitchelper.ui.theme.profile.GRIS_TEXTO
import com.example.kitchelper.ui.theme.profile.NARANJA
import com.example.kitchelper.ui.theme.profile.VERDE_KITCHEN
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

val AMARILLO_ESTRELLA = Color(0xFFFFC107)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(recetaId: String, navController: NavController) {
    var receta by remember { mutableStateOf<Receta?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var esFavorito by remember { mutableStateOf(false) }
    var tabSeleccionada by remember { mutableStateOf("Instrucciones") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(recetaId) {
        try {
            val doc = FirebaseFirestore.getInstance().collection("recetas").document(recetaId).get().await()
            receta = doc.toObject(Receta::class.java)?.copy(id = doc.id)

            if (userId.isNotBlank()) {
                val favDoc = FirebaseFirestore.getInstance().collection("favoritos").document("${userId}_${recetaId}").get().await()
                esFavorito = favDoc.exists()
            }
        } catch (_: Exception) {}
        isLoading = false
    }

    fun toggleFavorito() {
        if (userId.isBlank()) {
            android.widget.Toast.makeText(context, "Debes iniciar sesión para guardar favoritos", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        val estadoPrevio = esFavorito
        esFavorito = !esFavorito
        scope.launch {
            val result = FavoriteRepository().toggleFavorito(userId, recetaId)
            result.onSuccess { estadoFinal ->
                esFavorito = estadoFinal
                val msg = if (estadoFinal) "¡Agregado a tus favoritos!" else "Eliminado de tus favoritos"
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
            }.onFailure { err ->
                esFavorito = estadoPrevio
                android.widget.Toast.makeText(context, "Error al actualizar favorito: ${err.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CREMA)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VERDE_KITCHEN)
            }
        } else if (receta == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Receta no encontrada", fontFamily = InterFamily, color = Color.Gray)
            }
        } else {
            val currentReceta = receta!!

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 110.dp)
            ) {
                // ===== 1. HEADER CON BOTÓN ATRÁS Y AVATAR =====
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = VERDE_KITCHEN,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { navController.popBackStack() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Atrás",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Nivel - ${currentReceta.dificultad.ifBlank { "Principiante" }}",
                                    fontFamily = InterFamily,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    "KITCHELPER",
                                    fontFamily = FrauncesFamily,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VERDE_KITCHEN
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(2.dp, VERDE_KITCHEN),
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
                                    color = VERDE_KITCHEN
                                )
                            }
                        }
                    }
                }

                // ===== 2. HERO IMAGE BANNER =====
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(250.dp),
                        shadowElevation = 4.dp
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (currentReceta.imagenUrl.isNotBlank()) {
                                AsyncImage(
                                    model = currentReceta.imagenUrl,
                                    contentDescription = currentReceta.titulo,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.DarkGray)
                                )
                            }

                            // Degradado difuminado
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.3f),
                                                Color.Black.copy(alpha = 0.92f)
                                            )
                                        )
                                    )
                            )

                            // Ícono de estrella interactivo (Amarillo si es favorito)
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .size(38.dp)
                                    .clickable { toggleFavorito() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (esFavorito) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Favorito",
                                        tint = if (esFavorito) AMARILLO_ESTRELLA else Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Textos superpuestos
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(18.dp)
                            ) {
                                Text(
                                    currentReceta.categoria.ifBlank { "RECETA DESTACADA" }.uppercase(),
                                    fontFamily = InterFamily,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    currentReceta.titulo,
                                    fontFamily = InriaSerifFamily,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // ===== 3. METRICAS / INFORMACIÓN RÁPIDA (TIEMPO, PLATOS, DIFICULTAD) =====
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MetricaItem(
                                icon = Icons.Default.Schedule,
                                label = "${currentReceta.tiempoMinutos} MIN."
                            )
                            VerticalDivider(
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(1.dp),
                                color = Color(0xFFE0E0E0)
                            )
                            MetricaItem(
                                icon = Icons.Default.DinnerDining,
                                label = "2 PLATOS"
                            )
                            VerticalDivider(
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(1.dp),
                                color = Color(0xFFE0E0E0)
                            )
                            MetricaItem(
                                icon = Icons.Default.Restaurant,
                                label = currentReceta.dificultad.ifBlank { "FÁCIL" }.uppercase()
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // ===== 4. PÍLDORAS DE NAVEGACIÓN VISTA (INSTRUCCIONES / VIDEO / REALIDAD AR) =====
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = tabSeleccionada == "Instrucciones",
                            onClick = { tabSeleccionada = "Instrucciones" },
                            label = { Text("Instrucciones", fontFamily = InterFamily, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VERDE_KITCHEN,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = GRIS_TEXTO
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = tabSeleccionada == "Video",
                            onClick = {
                                tabSeleccionada = "Video"
                                if (currentReceta.videoUrl.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentReceta.videoUrl))
                                    context.startActivity(intent)
                                }
                            },
                            label = { Text("Video", fontFamily = InterFamily, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VERDE_KITCHEN,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = GRIS_TEXTO
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = tabSeleccionada == "Realidad AR",
                            onClick = {
                                tabSeleccionada = "Realidad AR"
                                navController.navigate(Routes.AR_GUIDE)
                            },
                            label = { Text("Realidad AR", fontFamily = InterFamily, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VERDE_KITCHEN,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = GRIS_TEXTO
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // ===== 5. SECCIÓN INGREDIENTES =====
                item {
                    Text(
                        "Ingredientes",
                        fontFamily = InriaSerifFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = GRIS_TEXTO,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(currentReceta.ingredientes) { ingrediente ->
                    ItemIngredienteDetalleCard(ingrediente = ingrediente)
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }

                // ===== 6. SECCIÓN A PREPARAR! (PASOS DE PREPARACIÓN) =====
                item {
                    Text(
                        "A preparar!",
                        fontFamily = InriaSerifFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = GRIS_TEXTO,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(currentReceta.pasos) { paso ->
                    ItemPasoDetalleCard(paso = paso)
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

// ============ MÉTRICA ITEM ============
@Composable
private fun MetricaItem(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = NARANJA,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            fontFamily = InterFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = GRIS_TEXTO
        )
    }
}

// ============ TARJETA DE INGREDIENTE EN DETALLE ============
@Composable
fun ItemIngredienteDetalleCard(ingrediente: Ingrediente) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF0F0F0),
                modifier = Modifier.size(60.dp)
            ) {
                if (ingrediente.imagenUrl.isNotBlank()) {
                    AsyncImage(
                        model = ingrediente.imagenUrl,
                        contentDescription = ingrediente.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    ingrediente.nombre.ifBlank { "Ingrediente" },
                    fontFamily = InriaSerifFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GRIS_TEXTO
                )
                Text(
                    "Detalles adicionales opcionales",
                    fontFamily = InterFamily,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEBEBEB)
                ) {
                    Text(
                        ingrediente.cantidad.ifBlank { "Al gusto" },
                        fontFamily = InterFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

// ============ TARJETA DE PASO EN DETALLE ============
@Composable
fun ItemPasoDetalleCard(paso: Paso) {
    val esPar = paso.orden % 2 == 0
    val backgroundColor = if (esPar) Color(0xFFB86400) else VERDE_KITCHEN

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "PASO ${paso.orden} - PREPARACIÓN",
                fontFamily = InterFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                paso.descripcion,
                fontFamily = InterFamily,
                fontSize = 13.sp,
                color = Color.White,
                lineHeight = 18.sp
            )

            if (paso.imagenUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = paso.imagenUrl,
                    contentDescription = "Paso ${paso.orden}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}
