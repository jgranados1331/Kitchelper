package com.example.kitchelper.ui.theme.practices

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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.example.kitchelper.data.model.PasoPractica
import com.example.kitchelper.data.model.PracticaBasica
import com.example.kitchelper.ui.theme.FrauncesFamily
import com.example.kitchelper.ui.theme.InterFamily
import com.example.kitchelper.ui.theme.InriaSerifFamily
import com.example.kitchelper.ui.theme.components.BarraNavegacionInferior
import com.example.kitchelper.ui.theme.navigation.Routes
import com.example.kitchelper.ui.theme.profile.CREMA
import com.example.kitchelper.ui.theme.profile.GRIS_TEXTO
import com.example.kitchelper.ui.theme.profile.NARANJA
import com.example.kitchelper.ui.theme.profile.VERDE_KITCHEN
import kotlinx.coroutines.tasks.await

@Composable
fun PracticeDetailScreen(practicaId: String, navController: NavController) {
    var practica by remember { mutableStateOf<PracticaBasica?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(practicaId) {
        try {
            val doc = FirebaseFirestore.getInstance().collection("practicas_basicas").document(practicaId).get().await()
            practica = doc.toObject(PracticaBasica::class.java)?.copy(id = doc.id)
        } catch (_: Exception) {}
        isLoading = false
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
        } else if (practica == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Práctica técnica no encontrada", fontFamily = InterFamily, color = Color.Gray)
            }
        } else {
            val currentPractica = practica!!

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
                                    "Nivel - ${currentPractica.dificultad.ifBlank { "Principiante" }}",
                                    fontFamily = InterFamily,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    "PRÁCTICA TÉCNICA",
                                    fontFamily = FrauncesFamily,
                                    fontSize = 20.sp,
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
                            .height(240.dp),
                        shadowElevation = 4.dp
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (currentPractica.imagenUrl.isNotBlank()) {
                                AsyncImage(
                                    model = currentPractica.imagenUrl,
                                    contentDescription = currentPractica.titulo,
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

                            // Degradado
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

                            // Título
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(18.dp)
                            ) {
                                Text(
                                    "TÉCNICA DE COCINA",
                                    fontFamily = InterFamily,
                                    fontSize = 11.sp,
                                    color = Color(0xFFC5D86D),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    currentPractica.titulo,
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

                // ===== 3. METRICAS / INFORMACIÓN RÁPIDA =====
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
                            MetricaPracticaItem(
                                icon = Icons.Default.Schedule,
                                label = "${currentPractica.tiempoMinutos} MIN."
                            )
                            VerticalDivider(
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(1.dp),
                                color = Color(0xFFE0E0E0)
                            )
                            MetricaPracticaItem(
                                icon = Icons.Default.Build,
                                label = "${currentPractica.pasos.size} PASOS"
                            )
                            VerticalDivider(
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(1.dp),
                                color = Color(0xFFE0E0E0)
                            )
                            MetricaPracticaItem(
                                icon = Icons.Default.Restaurant,
                                label = currentPractica.dificultad.ifBlank { "FÁCIL" }.uppercase()
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // ===== 4. SECCIÓN DESCRIPCIÓN =====
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Objetivo de la técnica",
                                fontFamily = InriaSerifFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = NARANJA
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                currentPractica.descripcion,
                                fontFamily = InterFamily,
                                fontSize = 13.sp,
                                color = GRIS_TEXTO,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // ===== 5. PASOS DE LA TÉCNICA =====
                item {
                    Text(
                        "Pasos de la técnica",
                        fontFamily = InriaSerifFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = GRIS_TEXTO,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(currentPractica.pasos) { paso ->
                    ItemPasoPracticaCard(paso = paso)
                }

                // ===== 6. ERRORES COMUNES (SI TIENE) =====
                if (currentPractica.erroresComunes.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    item {
                        Text(
                            "Errores comunes a evitar",
                            fontFamily = InriaSerifFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GRIS_TEXTO,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(currentPractica.erroresComunes) { errorTxt ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFF0EC),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = NARANJA,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    errorTxt,
                                    fontFamily = InterFamily,
                                    fontSize = 13.sp,
                                    color = GRIS_TEXTO
                                )
                            }
                        }
                    }
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

@Composable
private fun MetricaPracticaItem(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = NARANJA,
            modifier = Modifier.size(22.dp)
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

@Composable
fun ItemPasoPracticaCard(paso: PasoPractica) {
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
                "PASO ${paso.orden} - TÉCNICA",
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
        }
    }
}
