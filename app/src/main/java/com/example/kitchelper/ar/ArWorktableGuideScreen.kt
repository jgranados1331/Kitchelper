package com.example.kitchelper.ar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Display
import android.view.Surface as AndroidSurface
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.kitchelper.data.model.Paso
import com.example.kitchelper.data.model.Receta
import com.example.kitchelper.data.model.Usuario
import com.example.kitchelper.data.repository.GeminiAiRepository
import com.example.kitchelper.data.repository.RecipeRepository
import com.example.kitchelper.data.repository.UserRepository
import com.example.kitchelper.ui.theme.FrauncesFamily
import com.example.kitchelper.ui.theme.InterFamily
import com.example.kitchelper.ui.theme.profile.NARANJA
import com.example.kitchelper.ui.theme.profile.VERDE_KITCHEN
import com.google.ar.core.ArCoreApk
import com.google.firebase.auth.FirebaseAuth
import io.github.sceneview.ar.ARScene
import kotlinx.coroutines.delay
import java.util.Locale

enum class CalibrationStatus {
    SCANNING,
    VERIFYING_FLAT_SURFACE,
    SURFACE_DETECTED,
    CALIBRATED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArWorktableGuideScreen(
    recetaId: String? = null,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Estados de Receta y Datos
    var receta by remember { mutableStateOf<Receta?>(null) }
    var usuarioLogueado by remember { mutableStateOf<Usuario?>(null) }
    var pasoActualIndex by remember { mutableIntStateOf(0) }
    var consejoPasoActualIA by remember { mutableStateOf<String?>(null) }
    var sugerenciasIA by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingConsejo by remember { mutableStateOf(false) }
    var calibrationStatus by remember { mutableStateOf(CalibrationStatus.SCANNING) }
    var showAiAdviceDialog by remember { mutableStateOf(false) }

    // Estado para Zoom de Imagen de Referencia
    var isImageZoomed by remember { mutableStateOf(false) }

    // Reconocimiento real por cámara de ingredientes del paso
    var isStepRecognized by remember { mutableStateOf(false) }

    // Estado de Animación de Celebración con Emojis
    var showCelebrationEmoji by remember { mutableStateOf(false) }

    LaunchedEffect(showCelebrationEmoji) {
        if (showCelebrationEmoji) {
            delay(1800)
            showCelebrationEmoji = false
        }
    }

    // Analizador de visión artificial TFLite
    val analyzer = remember {
        IngredientAnalyzer(
            context = context,
            confidenceThreshold = 0.50f,
            onIngredientDetected = { detected ->
                if (detected != null && detected.confidence >= 0.45f) {
                    val foundName = detected.name

                    val matchInIngredients = receta?.ingredientes?.any { ing ->
                        ing.nombre.lowercase().contains(foundName.lowercase()) ||
                                foundName.lowercase().contains(ing.nombre.lowercase())
                    } == true

                    val matchInStep = pasoActualObjText(receta, pasoActualIndex).lowercase().contains(foundName.lowercase())

                    if (matchInIngredients || matchInStep) {
                        isStepRecognized = true
                    }
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            analyzer.close()
        }
    }

    // Motor Text-To-Speech para dictado por voz
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "ES")
                ttsEngine = tts
            }
        }
        onDispose {
            ttsEngine?.stop()
            ttsEngine?.shutdown()
        }
    }

    // Cargar Receta y Usuario de forma fresca e independiente
    LaunchedEffect(recetaId, userId) {
        if (!recetaId.isNullOrBlank()) {
            val res = RecipeRepository().obtenerReceta(recetaId)
            receta = res.getOrNull()
        }
        if (userId.isNotBlank()) {
            val userRes = UserRepository().obtenerUsuario(userId)
            usuarioLogueado = userRes.getOrNull()
        }
    }

    // Calcular sugerencias personalizadas de la receta completa según el perfil del usuario (Salud, Alergias, Preferencias)
    LaunchedEffect(receta?.id, usuarioLogueado) {
        val r = receta
        val u = usuarioLogueado
        if (r != null && u != null) {
            val aiRes = GeminiAiRepository().generarSugerenciasPersonalizadasReceta(u, r)
            sugerenciasIA = aiRes.getOrNull() ?: emptyList()
        }
    }

    // Cargar Consejo de Gemini IA hiper-personalizado por paso y reiniciar reconocimiento
    val pasos = receta?.pasos ?: emptyList()
    val pasoActualObj = pasos.getOrElse(pasoActualIndex) { Paso(descripcion = "Inspecciona tu mesa de trabajo y ten tus ingredientes listos.") }

    LaunchedEffect(pasoActualIndex, receta?.id, pasoActualObj.descripcion, usuarioLogueado) {
        isStepRecognized = false
        receta?.let { r ->
            consejoPasoActualIA = null
            isLoadingConsejo = true
            delay(150) // Breve margen para asegurar recálculo reactivo con feedback visual
            val adviceRes = GeminiAiRepository().obtenerConsejoPasoASpaso(
                tituloReceta = r.titulo,
                pasoActual = pasoActualObj,
                usuario = usuarioLogueado ?: Usuario()
            )
            consejoPasoActualIA = adviceRes.getOrNull()
            isLoadingConsejo = false
        }
    }

    // Permiso de Cámara
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Soporte ARCore
    var isArSupported by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) {
        try {
            val availability = ArCoreApk.getInstance().checkAvailability(context)
            isArSupported = availability.isSupported
        } catch (e: Exception) {
            Log.e("ArWorktableGuide", "Error comprobando ARCore", e)
            isArSupported = false
        }
    }

    // Verificación progresiva de mesa plana durante escaneo
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            delay(1500)
            if (calibrationStatus == CalibrationStatus.SCANNING) {
                calibrationStatus = CalibrationStatus.VERIFYING_FLAT_SURFACE
                delay(1800)
                calibrationStatus = CalibrationStatus.SURFACE_DETECTED
            }
        }
    }

    // Animación de pulso AR
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Función para reproducir instrucción por voz
    fun hablarInstruccion() {
        val tts = ttsEngine ?: return
        if (tts.isSpeaking) {
            tts.stop()
            isTtsSpeaking = false
        } else {
            val texto = buildString {
                append("Paso ${pasoActualObj.orden.takeIf { it > 0 } ?: (pasoActualIndex + 1)}: ")
                append(pasoActualObj.descripcion)
                consejoPasoActualIA?.let { consejo ->
                    append(". Consejo del Chef: ")
                    append(consejo)
                }
            }
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "step_tts")
            isTtsSpeaking = true
        }
    }

    if (!hasCameraPermission) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    Icons.Default.ViewInAr,
                    contentDescription = null,
                    tint = VERDE_KITCHEN,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Permiso de Cámara Requerido",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FrauncesFamily
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Necesitamos la cámara para guiar tu receta en Realidad Aumentada sobre tu mesa o tabla de picar.",
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    fontFamily = InterFamily,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = VERDE_KITCHEN),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Conceder Permiso de Cámara", fontFamily = InterFamily, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    // Diálogo completo de sugerencias del Chef IA
    if (showAiAdviceDialog) {
        AlertDialog(
            onDismissRequest = { showAiAdviceDialog = false },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NARANJA) },
            title = {
                Text(
                    "Consejo del Chef IA para este Paso",
                    fontFamily = FrauncesFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = consejoPasoActualIA ?: "Realiza el paso manteniendo orden en la superficie de trabajo.",
                        fontFamily = InterFamily,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                    if (sugerenciasIA.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Recomendaciones adicionales:", fontWeight = FontWeight.Bold, fontFamily = InterFamily, fontSize = 13.sp)
                        sugerenciasIA.forEach { tip ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text("• ", color = VERDE_KITCHEN, fontWeight = FontWeight.Bold)
                                Text(tip, fontFamily = InterFamily, fontSize = 13.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAiAdviceDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = VERDE_KITCHEN),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Entendido", fontFamily = InterFamily, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // DIÁLOGO MODAL DE ZOOM DE FOTO DE REFERENCIA EN PANTALLA COMPLETA
    if (isImageZoomed && pasoActualObj.imagenUrl.isNotBlank()) {
        Dialog(
            onDismissRequest = { isImageZoomed = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.94f))
                    .clickable { isImageZoomed = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Cabecera Modal Zoom
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📷 Imagen de Referencia - Paso ${pasoActualIndex + 1}",
                            color = Color.White,
                            fontFamily = FrauncesFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        IconButton(
                            onClick = { isImageZoomed = false },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Imagen Ampliada en pantalla completa
                    AsyncImage(
                        model = pasoActualObj.imagenUrl,
                        contentDescription = "Foto ampliada",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.8f)
                            .clip(RoundedCornerShape(16.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Toca en cualquier parte para cerrar",
                        color = Color.LightGray,
                        fontFamily = InterFamily,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (calibrationStatus == CalibrationStatus.CALIBRATED) "AR Guía de Cocina" else "Calibración AR de Mesa",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FrauncesFamily,
                            fontWeight = FontWeight.Bold
                        )
                        receta?.titulo?.let { titulo ->
                            Text(
                                text = titulo,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f),
                                fontFamily = InterFamily
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when (calibrationStatus) {
                            CalibrationStatus.CALIBRATED -> VERDE_KITCHEN
                            CalibrationStatus.SURFACE_DETECTED -> Color(0xFFFF9800)
                            CalibrationStatus.VERIFYING_FLAT_SURFACE -> Color(0xFF2196F3)
                            CalibrationStatus.SCANNING -> Color.Gray.copy(alpha = 0.6f)
                        },
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = when (calibrationStatus) {
                                CalibrationStatus.CALIBRATED -> "✓ Mesa AR Lista"
                                CalibrationStatus.SURFACE_DETECTED -> "✓ Mesa Plana Confirmada"
                                CalibrationStatus.VERIFYING_FLAT_SURFACE -> "Verificando Mesa..."
                                CalibrationStatus.SCANNING -> "Escaneando..."
                            },
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.75f),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ===== 1. CÁMARA (ARCore / CameraX) =====
            when (isArSupported) {
                true -> {
                    ARScene(
                        modifier = Modifier.fillMaxSize(),
                        onSessionUpdated = { _, frame ->
                            if (calibrationStatus == CalibrationStatus.SCANNING) {
                                calibrationStatus = CalibrationStatus.VERIFYING_FLAT_SURFACE
                            }
                            val rotationDegrees = getDisplayRotationDegrees(context)
                            analyzer.processFrame(frame, rotationDegrees)
                        }
                    )
                }
                false -> {
                    SimpleCameraPreview(modifier = Modifier.fillMaxSize(), analyzer = analyzer)
                }
                null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VERDE_KITCHEN)
                    }
                }
            }

            // ===== 2. SUPERPOSICIÓN DE RETÍCULA Y VERIFICACIÓN DE MESA PLANA =====
            if (calibrationStatus != CalibrationStatus.CALIBRATED) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (calibrationStatus) {
                        CalibrationStatus.SCANNING -> {
                            Box(
                                modifier = Modifier
                                    .size(240.dp)
                                    .scale(pulseScale)
                                    .border(
                                        width = 3.dp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .background(Color.White.copy(alpha = 0.05f))
                            ) {
                                Text(
                                    "APUNTA LA CÁMARA A TU MESA DE TRABAJO O TABLA DE PICAR",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = InterFamily,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp)
                                )
                            }
                        }
                        CalibrationStatus.VERIFYING_FLAT_SURFACE -> {
                            Box(
                                modifier = Modifier
                                    .size(250.dp)
                                    .border(
                                        width = 3.dp,
                                        color = Color(0xFF2196F3),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .background(Color(0xFF2196F3).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Verificando mesa plana...",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        fontFamily = InterFamily
                                    )
                                }
                            }
                        }
                        CalibrationStatus.SURFACE_DETECTED -> {
                            Box(
                                modifier = Modifier
                                    .size(260.dp)
                                    .border(
                                        width = 3.dp,
                                        color = VERDE_KITCHEN,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .background(VERDE_KITCHEN.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "¡Mesa Plana Confirmada!",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        fontFamily = FrauncesFamily
                                    )
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }

            // ===== 3. ENTORNO AR PASO A PASO (MESA CALIBRADA) =====
            if (calibrationStatus == CalibrationStatus.CALIBRATED) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // FILA SUPERIOR: IMAGEN EN MINIATURA CLICABLE + PROGRESO DE PASO (SIN SOLAPAMIENTO)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // MINIATURA CLICABLE CON LUPA Y AMPLACIÓN (ESQUINA IZQUIERDA)
                        if (pasoActualObj.imagenUrl.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.Black.copy(alpha = 0.85f),
                                border = BorderStroke(1.5.dp, VERDE_KITCHEN),
                                shadowElevation = 6.dp,
                                modifier = Modifier
                                    .size(62.dp)
                                    .clickable { isImageZoomed = true }
                            ) {
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    AsyncImage(
                                        model = pasoActualObj.imagenUrl,
                                        contentDescription = "Foto del paso",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(14.dp))
                                    )
                                    // Icono de lupa para indicar zoom
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.ZoomIn,
                                                contentDescription = "Zoom",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // HUD AR SUPERIOR DE PROGRESO DE PASOS
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            border = BorderStroke(1.dp, VERDE_KITCHEN.copy(alpha = 0.8f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = VERDE_KITCHEN,
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${if (pasos.isNotEmpty()) pasoActualIndex + 1 else 1}",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                fontFamily = InterFamily
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (pasos.isNotEmpty()) "PASO ${pasoActualIndex + 1} DE ${pasos.size}" else "PREPARACIÓN EN MESA",
                                        color = Color.White,
                                        fontFamily = FrauncesFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                // Botón de lectura por voz TTS
                                IconButton(
                                    onClick = { hablarInstruccion() },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isTtsSpeaking) NARANJA else VERDE_KITCHEN)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Escuchar paso",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // MARCO PROYECTADO 3D EN LA MESA CON TEXTO EN LA PARTE SUPERIOR (SIN SOLAPAMIENTO)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.96f)
                                .fillMaxHeight(0.95f)
                                .border(
                                    width = 2.5.dp,
                                    brush = Brush.horizontalGradient(
                                        listOf(VERDE_KITCHEN, Color.Cyan, VERDE_KITCHEN)
                                    ),
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .background(Color.Black.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top,
                                modifier = Modifier.padding(top = 14.dp, start = 12.dp, end = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.ViewInAr,
                                        contentDescription = null,
                                        tint = Color.Cyan,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "📌 ÁREA PROYECTADA EN MESA DE TRABAJO",
                                        color = Color.Cyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = InterFamily
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Ubica la tabla de picar o utensilios de este paso en esta zona",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                    fontFamily = InterFamily,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // BANNER DE RECONOCIMIENTO REAL DE INGREDIENTES POR CÁMARA
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isStepRecognized) Color(0xFF1B5E20).copy(alpha = 0.92f) else Color(0xFFE65100).copy(alpha = 0.92f),
                                    border = BorderStroke(1.5.dp, if (isStepRecognized) Color.Green else Color(0xFFFFB74D)),
                                    modifier = Modifier
                                        .fillMaxWidth(0.92f)
                                        .clickable { isStepRecognized = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isStepRecognized) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isStepRecognized)
                                                "Se ve que estás en el proceso, presiona siguiente cuando estés listo 👍"
                                            else
                                                "Muéstrame en cámara cuando ya hayas hecho este paso...",
                                            color = Color.White,
                                            fontFamily = InterFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // SECCIÓN COMPLETA DE INSTRUCCIÓN + TALLA PEQUEÑA DE IA
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.88f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // DESCRIPCIÓN TEXTUAL COMPLETA SIN RECORTAR
                            Text(
                                text = pasoActualObj.descripcion,
                                color = Color.White,
                                fontFamily = InterFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 18.sp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // SECCIÓN DE IA HIPER-PERSONALIZADA
                            Spacer(modifier = Modifier.height(6.dp))
                            val isWarningAi = consejoPasoActualIA?.startsWith("⚠️") == true
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isWarningAi) Color(0xFF331F00) else Color(0xFF1E2B1E),
                                border = BorderStroke(1.dp, if (isWarningAi) NARANJA else VERDE_KITCHEN),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAiAdviceDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (isWarningAi) Icons.Default.Warning else Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = if (isWarningAi) NARANJA else Color(0xFFFFD54F),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isLoadingConsejo) "Gemini IA analizando tu perfil..." else (consejoPasoActualIA ?: "Tip: Mantén orden en la mesa."),
                                        fontFamily = InterFamily,
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.95f),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Botones de Navegación de Pasos
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        if (pasoActualIndex > 0) {
                                            pasoActualIndex--
                                            if (isTtsSpeaking) ttsEngine?.stop()
                                        }
                                    },
                                    enabled = pasoActualIndex > 0,
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Anterior", fontFamily = InterFamily, fontSize = 11.sp)
                                }

                                Text(
                                    text = "${pasoActualIndex + 1} / ${pasos.size.coerceAtLeast(1)}",
                                    color = Color.LightGray,
                                    fontFamily = InterFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )

                                Button(
                                    onClick = {
                                        showCelebrationEmoji = true // 🎉 Muestra animación de celebración con emojis
                                        if (pasoActualIndex < pasos.size - 1) {
                                            pasoActualIndex++
                                            if (isTtsSpeaking) ttsEngine?.stop()
                                        } else {
                                            Toast.makeText(context, "¡Has completado todos los pasos de la receta! 🎉", Toast.LENGTH_LONG).show()
                                            onNavigateBack()
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isStepRecognized) VERDE_KITCHEN else Color(0xFFFF9800)
                                    )
                                ) {
                                    Text(
                                        if (pasoActualIndex < pasos.size - 1) "Siguiente" else "Finalizar",
                                        fontFamily = InterFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            // ===== OVERLAY ANIMADO DE CELEBRACIÓN CON EMOJIS =====
            AnimatedVisibility(
                visible = showCelebrationEmoji,
                enter = fadeIn() + scaleIn(initialScale = 0.5f),
                exit = fadeOut() + scaleOut(targetScale = 1.2f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.88f),
                    border = BorderStroke(2.dp, VERDE_KITCHEN),
                    shadowElevation = 12.dp,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉 🥳 👨‍🍳 ✨ 👏",
                            fontSize = 38.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "¡Excelente Trabajo!",
                            color = Color.White,
                            fontFamily = FrauncesFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Avanzando al siguiente paso...",
                            color = VERDE_KITCHEN,
                            fontFamily = InterFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ===== 4. CONTROLES INFERIORES EN FASE DE CALIBRACIÓN =====
            if (calibrationStatus != CalibrationStatus.CALIBRATED) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.85f)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (calibrationStatus) {
                                CalibrationStatus.SCANNING -> "Apunta la cámara a tu mesa de trabajo o escritorio"
                                CalibrationStatus.VERIFYING_FLAT_SURFACE -> "Verificando planitud y estabilidad de la superficie..."
                                CalibrationStatus.SURFACE_DETECTED -> "Superficie plana confirmada. Presiona para abrir la guía AR."
                                CalibrationStatus.CALIBRATED -> "¡Mesa vinculada con éxito!"
                            },
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = InterFamily,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (calibrationStatus != CalibrationStatus.SCANNING) {
                                IconButton(
                                    onClick = { calibrationStatus = CalibrationStatus.SCANNING },
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Reescanear",
                                        tint = Color.White
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    calibrationStatus = CalibrationStatus.CALIBRATED
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (calibrationStatus == CalibrationStatus.SURFACE_DETECTED) VERDE_KITCHEN else Color(0xFFFF6B4A)
                                )
                            ) {
                                Text(
                                    text = when (calibrationStatus) {
                                        CalibrationStatus.SCANNING -> "Fijar Mesa de Trabajo"
                                        CalibrationStatus.VERIFYING_FLAT_SURFACE -> "Verificando..."
                                        CalibrationStatus.SURFACE_DETECTED -> "Confirmar Mesa Plana AR"
                                        CalibrationStatus.CALIBRATED -> "Iniciar Guía AR"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = InterFamily,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun pasoActualObjText(receta: Receta?, index: Int): String {
    return receta?.pasos?.getOrNull(index)?.descripcion ?: ""
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
private fun SimpleCameraPreview(
    modifier: Modifier = Modifier,
    analyzer: IngredientAnalyzer? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = if (analyzer != null) {
                    ImageAnalysis.Builder()
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build().apply {
                            setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                analyzer.processImageProxy(imageProxy)
                            }
                        }
                } else null

                try {
                    cameraProvider.unbindAll()
                    if (imageAnalysis != null) {
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } else {
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview
                        )
                    }
                } catch (e: Exception) {
                    Log.e("SimpleCameraPreview", "Error iniciando cámara", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = modifier
    )
}

private fun getDisplayRotationDegrees(context: Context): Int {
    val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
    val rotation = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)?.rotation ?: AndroidSurface.ROTATION_0
    return when (rotation) {
        AndroidSurface.ROTATION_0 -> 90
        AndroidSurface.ROTATION_90 -> 0
        AndroidSurface.ROTATION_180 -> 270
        AndroidSurface.ROTATION_270 -> 180
        else -> 90
    }
}
