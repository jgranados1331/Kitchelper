package com.example.kitchelper.ar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.ar.core.Frame
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Analizador de ingredientes con un modelo TFLite entrenado en Teachable Machine.
 *
 * - No depende de Google Play Services ni de ML Kit (funciona en Android 5+).
 * - Compatible con ARCore y CameraX (misma clase para ambos paths).
 * - Suavizado temporal: exige que la misma clase gane en ≥2 de los últimos 5 frames
 *   para eliminar el parpadeo típico de clasificadores por frame.
 * - Throttle a ~10 FPS para ahorrar batería sin perder interactividad.
 *
 * Formato del modelo esperado (export "Floating point" de Teachable Machine):
 *   - Input:  float32[1, 224, 224, 3], rango [0, 1]
 *   - Output: float32[1, N] (softmax)
 * Si entrenas con otro tamaño (ej. 192 o 160), no toques el código:
 * el analyzer lee `inputSize` del propio tensor.
 */
class IngredientAnalyzer(
    private val context: Context,
    private val confidenceThreshold: Float = 0.65f,
    private val onIngredientDetected: (DetectedIngredient?) -> Unit,
    private val onError: (Exception) -> Unit = {}
) {
    private val tag = "IngredientAnalyzer"
    private val isAnalyzing = AtomicBoolean(false)
    private val lastAnalysisTime = AtomicLong(0L)

    // ~10 inferencias/segundo
    private val analysisIntervalMs = 100L

    // Suavizado temporal
    private val recentDetections = ArrayDeque<DetectedIngredient>()
    private val maxRecentSize = 5
    private val minVotesForStable = 2

    private val labels: List<String> = loadLabels()
    private val interpreter: Interpreter = loadInterpreter()
    private val inputSize: Int = interpreter.getInputTensor(0).shape()[1]

    private val imageProcessor: ImageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(127.5f, 127.5f)) // [0, 255] → [-1, 1]
        .build()

    data class DetectedIngredient(
        val name: String,
        val confidence: Float
    )

    // ===================== CARGA DE ASSETS =====================

    private fun loadLabels(): List<String> {
        return try {
            context.assets.open("ingredients_labels.txt")
                .bufferedReader()
                .useLines { lines ->
                    lines.map { raw ->
                        // TM exporta "0 tomate" o "tomate". Soportamos ambos.
                        raw.trim().replaceFirst(Regex("^\\d+[\\s.:\\-]+"), "")
                    }.filter { it.isNotBlank() }.toList()
                }
        } catch (e: Exception) {
            Log.e(tag, "No se pudo cargar ingredients_labels.txt", e)
            onError(e)
            emptyList()
        }
    }

    private fun loadInterpreter(): Interpreter {
        val modelBuffer = FileUtil.loadMappedFile(context, "ingredients_model.tflite")
        return Interpreter(
            modelBuffer,
            Interpreter.Options().apply {
                setNumThreads(2)
            }
        )
    }

    // ===================== API PÚBLICA =====================

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    fun processImageProxy(imageProxy: ImageProxy) {
        val now = System.currentTimeMillis()
        if (now - lastAnalysisTime.get() < analysisIntervalMs) {
            imageProxy.close(); return
        }
        if (!isAnalyzing.compareAndSet(false, true)) {
            imageProxy.close(); return
        }
        lastAnalysisTime.set(now)

        try {
            // CameraX convierte perfectamente el formato YUV a Bitmap (evita fallos de color)
            val bitmap = imageProxy.toBitmap()
            if (bitmap != null) {
                analyzeBitmap(bitmap)
                bitmap.recycle()
            }
        } catch (e: Exception) {
            Log.e(tag, "Error en processImageProxy", e)
            onError(e)
        } finally {
            imageProxy.close()
            isAnalyzing.set(false)
        }
    }

    fun processFrame(frame: Frame, rotationDegrees: Int) {
        val now = System.currentTimeMillis()
        if (now - lastAnalysisTime.get() < analysisIntervalMs) return
        if (!isAnalyzing.compareAndSet(false, true)) return
        lastAnalysisTime.set(now)

        var cpuImage: Image? = null
        try {
            cpuImage = frame.acquireCameraImage() ?: return
            val bitmap = yuvImageToBitmap(cpuImage, rotationDegrees)
            if (bitmap != null) {
                analyzeBitmap(bitmap)
                bitmap.recycle()
            }
        } catch (e: Exception) {
            Log.e(tag, "Error en processFrame", e)
            onError(e)
        } finally {
            cpuImage?.close()
            isAnalyzing.set(false)
        }
    }

    fun close() {
        try { interpreter.close() } catch (_: Exception) {}
    }

    // ===================== INFERENCIA =====================

    private fun analyzeBitmap(bitmap: Bitmap) {
        if (labels.isEmpty()) return

        val tensorImage = TensorImage.fromBitmap(bitmap)
        val processed = imageProcessor.process(tensorImage)

        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(processed.buffer, output)
        val probs = output[0]

        var maxIndex = 0
        var maxProb = 0f
        for (i in probs.indices) {
            if (probs[i] > maxProb) {
                maxProb = probs[i]; maxIndex = i
            }
        }

        val rawLabel = labels.getOrNull(maxIndex) ?: return

        if (maxProb < confidenceThreshold || isNeutralLabel(rawLabel)) {
            onIngredientDetected(null)
            return
        }

        val translated = toSpanish(rawLabel)
        val detection = DetectedIngredient(translated, maxProb)

        recentDetections.addLast(detection)
        if (recentDetections.size > maxRecentSize) recentDetections.removeFirst()

        val votes = recentDetections.groupingBy { it.name }.eachCount()
        val winner = votes.maxByOrNull { it.value }

        if (winner != null && winner.value >= minVotesForStable) {
            val avgConf = recentDetections
                .filter { it.name == winner.key }
                .map { it.confidence }
                .average()
                .toFloat()
            onIngredientDetected(DetectedIngredient(winner.key, avgConf))
        } else {
            onIngredientDetected(detection)
        }
    }

    private fun isNeutralLabel(label: String): Boolean {
        val l = label.lowercase()
        return listOf(
            "fondo", "background", "mano", "hand",
            "sin_ingrediente", "ninguno", "nada", "utensilio", "utensil"
        ).any { l.contains(it) }
    }

    // ===================== CONVERSIÓN YUV → BITMAP =====================

    private fun yuvImageToBitmap(image: Image, rotationDegrees: Int): Bitmap? {
        return try {
            val nv21 = imageToNv21(image)
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 90, out)
            val bytes = out.toByteArray()
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null

            if (rotationDegrees != 0) {
                val matrix = android.graphics.Matrix().apply {
                    postRotate(rotationDegrees.toFloat())
                }
                val rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                if (rotated !== bmp) bmp.recycle()
                rotated
            } else {
                bmp
            }
        } catch (e: Exception) {
            Log.e(tag, "yuvImageToBitmap error", e)
            null
        }
    }

    /** Conversión robusta YUV_420_888 → NV21 respetando rowStride/pixelStride. */
    private fun imageToNv21(image: Image): ByteArray {
        val width = image.width
        val height = image.height
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val nv21 = ByteArray(width * height * 3 / 2)
        var pos = 0

        val yBuf = yPlane.buffer
        val yRowStride = yPlane.rowStride
        val yPixelStride = yPlane.pixelStride
        val yBufPos = yBuf.position()
        for (row in 0 until height) {
            for (col in 0 until width) {
                nv21[pos++] = yBuf.get(yBufPos + row * yRowStride + col * yPixelStride)
            }
        }

        val uvWidth = width / 2
        val uvHeight = height / 2
        val uBuf = uPlane.buffer
        val vBuf = vPlane.buffer
        val uRowStride = uPlane.rowStride
        val vRowStride = vPlane.rowStride
        val uPixelStride = uPlane.pixelStride
        val vPixelStride = vPlane.pixelStride
        val uBufPos = uBuf.position()
        val vBufPos = vBuf.position()

        for (row in 0 until uvHeight) {
            for (col in 0 until uvWidth) {
                nv21[pos++] = vBuf.get(vBufPos + row * vRowStride + col * vPixelStride)
                nv21[pos++] = uBuf.get(uBufPos + row * uRowStride + col * uPixelStride)
            }
        }
        return nv21
    }

    // ===================== MAPEO A ESPAÑOL =====================

    companion object {
        private val SPANISH_NAMES = mapOf(
            "tomato" to "Tomate", "onion" to "Cebolla", "garlic" to "Ajo",
            "potato" to "Papa", "carrot" to "Zanahoria", "pepper" to "Pimiento",
            "lettuce" to "Lechuga", "spinach" to "Espinaca",
            "broccoli" to "Brócoli", "cabbage" to "Repollo", "cauliflower" to "Coliflor",
            "zucchini" to "Calabacín", "cucumber" to "Pepino", "pumpkin" to "Calabaza",
            "mushroom" to "Champiñón",
            "apple" to "Manzana", "banana" to "Plátano", "orange" to "Naranja",
            "lemon" to "Limón", "lime" to "Lima", "strawberry" to "Fresa",
            "avocado" to "Aguacate",
            "cheese" to "Queso", "milk" to "Leche", "butter" to "Mantequilla",
            "egg" to "Huevo", "yogurt" to "Yogur",
            "meat" to "Carne", "beef" to "Res", "pork" to "Cerdo", "chicken" to "Pollo",
            "fish" to "Pescado", "salmon" to "Salmón", "tuna" to "Atún",
            "shrimp" to "Camarón", "bacon" to "Tocino", "ham" to "Jamón",
            "bread" to "Pan", "pasta" to "Pasta", "rice" to "Arroz",
            "flour" to "Harina", "bean" to "Frijol", "lentil" to "Lenteja",
            "oil" to "Aceite", "vinegar" to "Vinagre", "sauce" to "Salsa",
            "salt" to "Sal", "sugar" to "Azúcar", "honey" to "Miel",
            "chocolate" to "Chocolate"
        )

        fun toSpanish(rawLabel: String): String {
            val key = rawLabel.lowercase().trim()
            SPANISH_NAMES[key]?.let { return it }
            val tokens = key.split(" ", "_", "-").reversed()
            for (t in tokens) SPANISH_NAMES[t]?.let { return it }
            return rawLabel.replace("_", " ").replace("-", " ")
                .split(" ")
                .joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
        }
    }
}