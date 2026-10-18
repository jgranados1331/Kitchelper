package com.example.kitchelper.ar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.util.Log
import android.view.Display
import android.view.Surface
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.ar.core.ArCoreApk
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import io.github.sceneview.ar.ARScene
import com.google.common.util.concurrent.ListenableFuture


/**
 * Pantalla interactiva para escaneo y registro manual de ingredientes con ARCore / CameraX.
 *
 * Muestra el ingrediente enfocado actualmente en tiempo real y permite guardarlo
 * únicamente cuando el usuario presiona explícitamente el botón "Registrar Ingrediente".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArIngredientScannerScreen(
    onNavigateBack: () -> Unit = {},
    onConfirm: (List<String>) -> Unit,
    viewModel: ArIngredientViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

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

    // Feedback visual mediante Toast al registrar un ingrediente
    LaunchedEffect(uiState.userFeedbackMessage) {
        uiState.userFeedbackMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearUserFeedback()
        }
    }

    // Verificación de compatibilidad con ARCore
    var isArSupported by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) {
        try {
            val availability = ArCoreApk.getInstance().checkAvailability(context)
            isArSupported = availability.isSupported
        } catch (e: Exception) {
            Log.e("ArIngredientScanner", "Error verificando ARCore", e)
            isArSupported = false
        }
    }

    if (!hasCameraPermission) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Se requiere permiso de cámara para escanear ingredientes.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Conceder Permiso")
                }
            }
        }
        return
    }

    // Instancia del analizador
    val analyzer = remember {
        IngredientAnalyzer(
            confidenceThreshold = 0.55f,
            onIngredientDetected = { detected ->
                viewModel.onIngredientDetected(detected)
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            analyzer.close()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isArSupported == true) "Escáner AR de Ingredientes" else "Escáner de Ingredientes",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Contador de ingredientes registrados
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${uiState.savedIngredients.size} guardados",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.6f),
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
            // 1. VISTA DE CÁMARA (ARCore o CameraX Fallback)
            when (isArSupported) {
                true -> {
                    ARScene(
                        modifier = Modifier.fillMaxSize(),
                        onSessionUpdated = { _, frame ->
                            val rotationDegrees = getDisplayRotationDegrees(context)
                            analyzer.processFrame(frame, rotationDegrees)
                        }
                    )
                }
                false -> {
                    CameraXScannerView(
                        modifier = Modifier.fillMaxSize(),
                        onIngredientDetected = { detected ->
                            viewModel.onIngredientDetected(detected)
                        }
                    )
                }
                null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            // 2. MIRA / RETÍCULA CENTRAL DE ENFOQUE
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.Center)
                    .border(
                        width = 2.dp,
                        color = if (uiState.currentIngredient != null) Color.Green else Color.White.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            )

            // 3. SUPERPOSICIÓN DE ESTADO Y BOTÓN DE REGISTRO
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tarjeta del Ingrediente Enfocado Actualmente
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "INGREDIENTE ENFOCADO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val current = uiState.currentIngredient
                        if (current != null) {
                            Text(
                                text = current.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Confianza: ${(current.confidence * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Green
                            )
                        } else {
                            Text(
                                text = "Apunta la cámara a un ingrediente...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Lista horizontal de ingredientes ya guardados
                AnimatedVisibility(
                    visible = uiState.savedIngredients.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Text(
                            text = "Ingredientes registrados para tu receta:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.savedIngredients) { item ->
                                InputChip(
                                    selected = true,
                                    onClick = { viewModel.removeSavedIngredient(item) },
                                    label = { Text(item) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = InputChipDefaults.inputChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }

                // BOTÓN "REGISTRAR INGREDIENTE" (Acción Manual explícita)
                Button(
                    onClick = { viewModel.registerCurrentIngredient() },
                    enabled = uiState.currentIngredient != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Registrar Ingrediente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // BOTÓN PARA TERMINAR Y DEVOLVER LOS INGREDIENTES A LA RECETA
                AnimatedVisibility(
                    visible = uiState.savedIngredients.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Button(
                        onClick = { onConfirm(uiState.savedIngredients) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .height(54.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A6B4A))
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Usar ${uiState.savedIngredients.size} ingredientes en la receta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Vista de fallback en CameraX para análisis con ML Kit.
 */
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
private fun CameraXScannerView(
    modifier: Modifier = Modifier,
    onIngredientDetected: (IngredientAnalyzer.DetectedIngredient?) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val labeler = remember {
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.55f)
            .build()
        ImageLabeling.getClient(options)
    }

    DisposableEffect(Unit) {
        onDispose {
            labeler.close()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        labeler.process(inputImage)
                            .addOnSuccessListener { labels ->
                                val foodLabels = labels
                                    .filter { IngredientAnalyzer.isFoodIngredient(it.text) }
                                    .sortedByDescending { it.confidence }

                                val topIngredient = foodLabels.firstOrNull()?.let {
                                    IngredientAnalyzer.DetectedIngredient(
                                        name = it.text.replaceFirstChar { char -> char.uppercase() },
                                        confidence = it.confidence
                                    )
                                }
                                onIngredientDetected(topIngredient)
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("CameraXScannerView", "Error al vincular la cámara con CameraX", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}

private fun getDisplayRotationDegrees(context: Context): Int {
    val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
    val rotation = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)?.rotation ?: Surface.ROTATION_0
    return when (rotation) {
        Surface.ROTATION_0 -> 90
        Surface.ROTATION_90 -> 0
        Surface.ROTATION_180 -> 270
        Surface.ROTATION_270 -> 180
        else -> 90
    }
}
