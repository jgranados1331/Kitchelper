package com.example.kitchelper.ui.theme.recipes

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.example.kitchelper.data.model.Paso
import com.example.kitchelper.data.model.Receta
import com.example.kitchelper.data.model.Usuario
import com.example.kitchelper.data.repository.GeminiAiRepository
import com.example.kitchelper.data.repository.RecipeRepository
import com.example.kitchelper.data.repository.UserRepository
import com.example.kitchelper.ui.theme.FrauncesFamily
import com.example.kitchelper.ui.theme.InterFamily
import com.example.kitchelper.ui.theme.InriaSerifFamily
import com.example.kitchelper.ui.theme.components.BarraNavegacionInferior
import com.example.kitchelper.ui.theme.navigation.Routes
import com.example.kitchelper.ui.theme.profile.CREMA
import com.example.kitchelper.ui.theme.profile.GRIS_TEXTO
import com.example.kitchelper.ui.theme.profile.NARANJA
import com.example.kitchelper.ui.theme.profile.VERDE_KITCHEN

@Composable
fun RecipeStepViewerScreen(recetaId: String, navController: NavController) {
    var receta by remember { mutableStateOf<Receta?>(null) }
    var usuarioLogueado by remember { mutableStateOf<Usuario?>(null) }
    var sugerenciasIA by remember { mutableStateOf<List<String>>(emptyList()) }
    var consejoPasoActualIA by remember { mutableStateOf<String?>(null) }
    var isLoadingConsejoPaso by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var pasoActualIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(recetaId) {
        try {
            val result = RecipeRepository().obtenerReceta(recetaId)
            receta = result.getOrNull()

            if (userId.isNotBlank()) {
                val userRes = UserRepository().obtenerUsuario(userId)
                usuarioLogueado = userRes.getOrNull()

                if (receta != null && usuarioLogueado != null) {
                    val aiRes = GeminiAiRepository().generarSugerenciasPersonalizadasReceta(usuarioLogueado!!, receta!!)
                    sugerenciasIA = aiRes.getOrNull() ?: emptyList()
                }
            }
        } catch (_: Exception) {}
        isLoading = false
    }

    // Efecto para solicitar a Gemini IA el consejo técnico para cada paso específico
    LaunchedEffect(pasoActualIndex, receta) {
        val currentReceta = receta
        if (currentReceta != null && currentReceta.pasos.isNotEmpty()) {
            val paso = currentReceta.pasos.getOrElse(pasoActualIndex) { currentReceta.pasos.first() }
            isLoadingConsejoPaso = true
            val aiStepRes = GeminiAiRepository().obtenerConsejoPasoASpaso(
                tituloReceta = currentReceta.titulo,
                pasoActual = paso,
                usuario = usuarioLogueado ?: Usuario()
            )
            consejoPasoActualIA = aiStepRes.getOrNull()
            isLoadingConsejoPaso = false
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
        } else if (receta == null || receta!!.pasos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (receta == null) "Receta no encontrada" else "Esta receta no tiene pasos asignados",
                    fontFamily = InterFamily,
                    color = Color.Gray
                )
            }
        } else {
            val currentReceta = receta!!
            val pasos = currentReceta.pasos
            val pasoActual: Paso = pasos.getOrElse(pasoActualIndex) { pasos.first() }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 110.dp)
            ) {
                // ===== HEADER =====
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
                                "PASO A PASO GUIADO",
                                fontFamily = InterFamily,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                currentReceta.titulo,
                                fontFamily = FrauncesFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = VERDE_KITCHEN,
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(2.dp, VERDE_KITCHEN),
                        modifier = Modifier.size(44.dp)
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

                Spacer(modifier = Modifier.height(8.dp))

                // ===== SECCIÓN 1: IMAGEN DEL PASO A PASO =====
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(220.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val imagenAMostrar = pasoActual.imagenUrl.ifBlank { currentReceta.imagenUrl }
                        if (imagenAMostrar.isNotBlank()) {
                            AsyncImage(
                                model = imagenAMostrar,
                                contentDescription = "Paso ${pasoActual.orden}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Sin imagen disponible", fontFamily = InterFamily, color = Color.Gray)
                            }
                        }

                        // Píldora del paso
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = VERDE_KITCHEN,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                "PASO ${pasoActualIndex + 1} DE ${pasos.size}",
                                fontFamily = InterFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ===== SECCIÓN 2: DESCRIPCIÓN CON FLECHAS LATERALES =====
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (pasoActualIndex > 0) pasoActualIndex--
                                },
                                enabled = pasoActualIndex > 0
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Paso Anterior",
                                    tint = if (pasoActualIndex > 0) VERDE_KITCHEN else Color.LightGray,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Text(
                                "Paso ${pasoActualIndex + 1} de ${pasos.size}",
                                fontFamily = InriaSerifFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = NARANJA
                            )

                            IconButton(
                                onClick = {
                                    if (pasoActualIndex < pasos.size - 1) pasoActualIndex++
                                },
                                enabled = pasoActualIndex < pasos.size - 1
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Paso Siguiente",
                                    tint = if (pasoActualIndex < pasos.size - 1) VERDE_KITCHEN else Color.LightGray,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { (pasoActualIndex + 1) / pasos.size.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = VERDE_KITCHEN,
                            trackColor = Color(0xFFE0E0E0)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            pasoActual.descripcion,
                            fontFamily = InterFamily,
                            fontSize = 15.sp,
                            color = GRIS_TEXTO,
                            lineHeight = 22.sp
                        )

                        if (pasoActual.videoUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val validUrl = if (!pasoActual.videoUrl.startsWith("http")) "https://${pasoActual.videoUrl}" else pasoActual.videoUrl
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "No se pudo abrir el video", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NARANJA),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ver video del paso", fontFamily = InterFamily, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ===== SECCIÓN 3: RECOMENDACIÓN DE LA IA SEGÚN EL PASO Y PERFIL =====
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF2F7EC),
                    border = BorderStroke(1.5.dp, VERDE_KITCHEN),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "IA",
                                tint = VERDE_KITCHEN,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Consejo del Chef IA para el Paso ${pasoActualIndex + 1}",
                                fontFamily = InriaSerifFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = VERDE_KITCHEN
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isLoadingConsejoPaso) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = VERDE_KITCHEN,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Consultando consejo del Chef IA para este paso...",
                                    fontFamily = InterFamily,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            Text(
                                consejoPasoActualIA ?: "👨‍🍳 Sigue las indicaciones del paso a paso y mantén un fuego constante.",
                                fontFamily = InterFamily,
                                fontSize = 13.sp,
                                color = GRIS_TEXTO,
                                lineHeight = 18.sp
                            )
                        }

                        if (sugerenciasIA.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFD0E0C0))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Adaptación a tu Perfil:",
                                fontFamily = InriaSerifFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NARANJA
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            sugerenciasIA.forEach { sugerencia ->
                                Text(
                                    sugerencia,
                                    fontFamily = InterFamily,
                                    fontSize = 12.sp,
                                    color = GRIS_TEXTO,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(vertical = 1.dp)
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
