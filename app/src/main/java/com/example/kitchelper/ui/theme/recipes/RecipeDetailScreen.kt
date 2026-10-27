package com.example.kitchelper.ui.theme.recipes

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Info
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
import com.example.kitchelper.data.model.Usuario
import com.example.kitchelper.data.repository.FavoriteRepository
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(recetaId: String, navController: NavController) {
    var receta by remember { mutableStateOf<Receta?>(null) }
    var usuarioLogueado by remember { mutableStateOf<Usuario?>(null) }
    var sugerenciasIA by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var esFavorito by remember { mutableStateOf(false) }
    var tabSeleccionada by remember { mutableStateOf("Instrucciones") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(recetaId) {
        try {
            val result = RecipeRepository().obtenerReceta(recetaId)
            receta = result.getOrNull()

            if (userId.isNotBlank()) {
                val favsResult = FavoriteRepository().obtenerIdsFavoritos(userId)
                val favSet = favsResult.getOrNull() ?: emptySet()
                esFavorito = favSet.contains(recetaId)

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

    fun toggleFavorito() {
        if (userId.isBlank()) {
            Toast.makeText(context, "Debes iniciar sesión para guardar favoritos", Toast.LENGTH_SHORT).show()
            return
        }
        val estadoPrevio = esFavorito
        esFavorito = !esFavorito
        scope.launch {
            val result = FavoriteRepository().toggleFavorito(userId, recetaId)
            result.onSuccess { estadoFinal ->
                esFavorito = estadoFinal
                val msg = if (estadoFinal) "¡Agregado a tus favoritos!" else "Eliminado de tus favoritos"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }.onFailure { err ->
                esFavorito = estadoPrevio
                Toast.makeText(context, "Error al actualizar favorito: ${err.message}", Toast.LENGTH_LONG).show()
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
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { navController.popBackStack() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Atrás",
                                        tint = MaterialTheme.colorScheme.surface,
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
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "KITCHELPER",
                                    fontFamily = FrauncesFamily,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White,
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

                            // Ícono de estrella interactivo
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
                                        tint = if (esFavorito) Color(0xFFFFC107) else Color.White,
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

                // ===== 3. METRICAS / INFORMACIÓN RÁPIDA =====
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            MetricaItem(
                                icon = Icons.Default.DinnerDining,
                                label = "2 PLATOS"
                            )
                            VerticalDivider(
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(1.dp),
                                color = MaterialTheme.colorScheme.onSurface,
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
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = tabSeleccionada == "Video",
                            onClick = {
                                tabSeleccionada = "Video"
                                if (currentReceta.videoUrl.isNotBlank()) {
                                    val videoLink = if (!currentReceta.videoUrl.startsWith("http")) "https://${currentReceta.videoUrl}" else currentReceta.videoUrl
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoLink))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "No se pudo abrir el enlace de video", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Esta receta no incluye un enlace de video de YouTube", Toast.LENGTH_SHORT).show()
                                }
                            },
                            label = { Text("Video", fontFamily = InterFamily, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface,
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
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // ===== TARJETA DE RECOMENDACIONES DEL CHEF IA =====
                if (sugerenciasIA.isNotEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFF2F7EC),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = "IA",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Consejos de Adaptación del Chef IA",
                                        fontFamily = InriaSerifFamily,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                sugerenciasIA.forEach { sugerencia ->
                                    Text(
                                        sugerencia,
                                        fontFamily = InterFamily,
                                        fontSize = 13.sp,
                                        color = GRIS_TEXTO,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                // ===== 5. SECCIÓN INGREDIENTES =====
                item {
                    Text(
                        "Ingredientes",
                        fontFamily = InriaSerifFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(currentReceta.ingredientes) { ingrediente ->
                    ItemIngredienteDetalleCard(ingrediente = ingrediente, ubicacionUsuario = "Colombia")
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }

                // ===== 6. SECCIÓN A PREPARAR! =====
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            "A preparar!",
                            fontFamily = InriaSerifFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Sigue la preparación paso a paso con fotos y asistencia guiada.",
                            fontFamily = InterFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { navController.navigate("recipe_step_viewer/${currentReceta.id}") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Text(
                                "COMENZAR PREPARACIÓN PASO A PASO ▶",
                                fontFamily = InterFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
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

// ============ MÉTRICA ITEM ============
@Composable
private fun MetricaItem(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
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

// ============ TARJETA DE INGREDIENTE EN DETALLE CON GEMINI IA ============
@Composable
fun ItemIngredienteDetalleCard(
    ingrediente: Ingrediente,
    ubicacionUsuario: String = "Colombia"
) {
    var showInfoDialog by remember { mutableStateOf(false) }
    var infoTextoIA by remember { mutableStateOf<String?>(null) }
    var isLoadingInfoIA by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(ingrediente.nombre) {
        infoTextoIA = null
    }

    fun abrirInfoConIA() {
        showInfoDialog = true
        isLoadingInfoIA = true
        scope.launch {
            val res = GeminiAiRepository().obtenerInfoIngredienteConUbicacion(ingrediente.nombre, ubicacionUsuario)
            infoTextoIA = res.getOrNull() ?: "Supermercados locales, plazas de mercado o tiendas de víveres en $ubicacionUsuario."
            isLoadingInfoIA = false
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
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
                    "Ingrediente de la receta",
                    fontFamily = InterFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        ingrediente.cantidad.ifBlank { "Al gusto" },
                        fontFamily = InterFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Botón de información (i) con Gemini IA
            Surface(
                shape = CircleShape,
                color = Color(0xFFF2F7EC),
                modifier = Modifier
                    .size(36.dp)
                    .clickable { abrirInfoConIA() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Información con IA del ingrediente",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Diálogo informativo del ingrediente con respuesta en tiempo real de Gemini IA
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "IA",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Información IA: ${ingrediente.nombre.ifBlank { "Ingrediente" }}",
                        fontFamily = InriaSerifFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            text = {
                Column {
                    if (isLoadingInfoIA) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Text(
                            infoTextoIA ?: "Cargando información del ingrediente...",
                            fontFamily = InterFamily,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Entendido", fontFamily = InterFamily, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

// ============ TARJETA DE PASO EN DETALLE ============
@Composable
fun ItemPasoDetalleCard(paso: Paso) {
    val context = LocalContext.current
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

            if (paso.videoUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.clickable {
                        val validUrl = if (!paso.videoUrl.startsWith("http")) "https://${paso.videoUrl}" else paso.videoUrl
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "No se pudo abrir el video del paso", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "▶ Ver video demostrativo de este paso",
                            fontFamily = InterFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
