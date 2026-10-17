package com.example.kitchelper.ar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.util.Log
import android.view.Display
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import io.github.sceneview.ar.ARScene

/**
 * Pantalla de escaneo de ingredientes adaptable.
 *
 * Si el dispositivo cuenta con soporte para ARCore, utiliza la vista de cámara AR.
 * Si el dispositivo NO soporta ARCore (o el APK de Google Play Services for AR no es compatible),
 * realiza un fallback automático a CameraX con ML Kit Image Labeling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArIngredientScannerScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
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

    // Comprobación de disponibilidad de ARCore en el dispositivo actual
    var isArSupported by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        try {
            val availability = ArCoreApk.getInstance().checkAvailability(context)
            isArSupported = availability.isSupported
        } catch (e: Exception) {
            Log.e("ArIngredientScanner", "Error al comprobar ARCore", e)
            isArSupported = false
        }
    }

    if (!hasCameraPermission) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Se requiere permiso de cámara para el escáner.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Conceder Permiso")
                }
            }
        }
        return
    }

    var detectedIngredients by remember { 
        mutableStateOf<List<IngredientAnalyzer.DetectedIngredient>>(emptyList()) 
    }

    val analyzer = remember {
        IngredientAnalyzer(
            confidenceThreshold = 0.6f,
            onIngredientsDetected = { ingredients ->
                detectedIngredients = ingredients
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
                        if (isArSupported == true) "Escáner AR de Ingredientes" 
                        else "Escáner de Ingredientes (Modo Estándar)"
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
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
            when (isArSupported) {
                true -> {
                    // Modo ARCore (para dispositivos soportados)
                    ARScene(
                        modifier = Modifier.fillMaxSize(),
                        onSessionUpdated = { _, frame ->
                            val rotationDegrees = getDisplayRotationDegrees(context)
                            analyzer.processFrame(frame, rotationDegrees)
                        }
                    )
                }
                false -> {
                    // Modo Fallback CameraX (para dispositivos o emuladores no compatibles con ARCore)
                    CameraXScannerView(
                        modifier = Modifier.fillMaxSize(),
                        onIngredientsDetected = { ingredients ->
                            detectedIngredients = ingredients
                        }
                    )
                }
                null -> {
                    // Cargando estado de verificación de ARCore
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Banner indicativo de modo en tiempo real
            if (isArSupported == false) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.DarkGray.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = "Modo cámara estándar activado (ARCore no soportado)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            // Superposición UI con los ingredientes detectados
            if (detectedIngredients.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.75f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Ingredientes Detectados:",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                            items(detectedIngredients) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${(item.confidence * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente CameraX para captura de cámara y análisis de imágenes con ML Kit
 * cuando ARCore no está disponible en el dispositivo.
 */
@OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
private fun CameraXScannerView(
    modifier: Modifier = Modifier,
    onIngredientsDetected: (List<IngredientAnalyzer.DetectedIngredient>) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val labeler = remember {
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.6f)
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
                                val detected = labels.map {
                                    IngredientAnalyzer.DetectedIngredient(it.text, it.confidence)
                                }
                                onIngredientsDetected(detected)
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
                    Log.e("CameraXScannerView", "Error al vincular cámara", e)
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
