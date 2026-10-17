package com.example.kitchelper.ar

import android.media.Image
import android.util.Log
import com.google.ar.core.Frame
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Componente encargada del análisis en tiempo real de fotogramas (frames) de ARCore
 * utilizando ML Kit Image Labeling.
 *
 * Implementa control de throttling y gestión estricta de memoria para evitar congelar
 * la transmisión de la cámara de ARCore debido al agotamiento del búfer de imágenes.
 */
class IngredientAnalyzer(
    private val confidenceThreshold: Float = 0.6f,
    private val onIngredientsDetected: (List<DetectedIngredient>) -> Unit,
    private val onError: (Exception) -> Unit = {}
) {
    private val tag = "IngredientAnalyzer"

    // Bandera atómica para evitar procesar múltiples fotogramas simultáneamente (Throttling)
    private val isAnalyzing = AtomicBoolean(false)

    // Cliente de ML Kit Image Labeling
    private val labeler: ImageLabeler by lazy {
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(confidenceThreshold)
            .build()
        ImageLabeling.getClient(options)
    }

    /**
     * Modelo de datos para representar un ingrediente u objeto detectado.
     */
    data class DetectedIngredient(
        val name: String,
        val confidence: Float
    )

    /**
     * Procesa un [Frame] de ARCore extraído de la sesión.
     *
     * @param frame Fotograma actual de ARCore.
     * @param rotationDegrees Compensación de rotación en grados (0, 90, 180, 270) según la orientación de la pantalla.
     */
    fun processFrame(frame: Frame, rotationDegrees: Int) {
        // 1. Throttling: Si ya se está analizando un fotograma, ignorar el actual
        if (!isAnalyzing.compareAndSet(false, true)) {
            return
        }

        var cpuImage: Image? = null
        try {
            // 2. Extracción de la imagen en formato YUV_420_888 desde el Frame de ARCore
            cpuImage = frame.acquireCameraImage()
            if (cpuImage == null) {
                isAnalyzing.set(false)
                return
            }

            // 3. Conversión de android.media.Image a InputImage de ML Kit considerando la rotación
            val inputImage = InputImage.fromMediaImage(cpuImage, rotationDegrees)

            // 4. Envío de la imagen al modelo de clasificación de ML Kit
            labeler.process(inputImage)
                .addOnSuccessListener { labels ->
                    val detectedList = labels.map { label ->
                        DetectedIngredient(
                            name = label.text,
                            confidence = label.confidence
                        )
                    }
                    onIngredientsDetected(detectedList)
                }
                .addOnFailureListener { exception ->
                    Log.e(tag, "Error en el análisis de imagen con ML Kit", exception)
                    onError(exception)
                }
                .addOnCompleteListener {
                    // 5. GESTIÓN DE MEMORIA CRÍTICA:
                    // Liberar siempre la imagen de la cámara para retornar el búfer a ARCore.
                    // Si no se llama a cpuImage.close(), la cola de imágenes de ARCore se agota y la cámara se congela.
                    cpuImage.close()
                    isAnalyzing.set(false)
                }

        } catch (e: Exception) {
            Log.e(tag, "Excepción al adquirir la imagen o procesar con ML Kit", e)
            cpuImage?.close()
            isAnalyzing.set(false)
        }
    }

    /**
     * Libera los recursos del clasificador cuando ya no sea necesario.
     */
    fun close() {
        labeler.close()
    }
}
