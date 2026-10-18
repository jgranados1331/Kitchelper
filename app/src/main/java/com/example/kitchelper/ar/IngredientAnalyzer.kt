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
 * Análisis en tiempo real de fotogramas ARCore con ML Kit Image Labeling.
 *
 * Estrategia de filtrado:
 *  1. Tokenizar la etiqueta devuelta por ML Kit (separada por espacios).
 *  2. Rechazar si CUALQUIER token está en la blacklist (cuerpo, muebles,
 *     categorías genéricas como "food", "fruit", "vegetable", "cuisine"…).
 *  3. Aceptar si ALGÚN token está en la whitelist de ingredientes específicos.
 *  4. En cualquier otro caso, rechazar (fail-safe).
 *
 * El matching es por token exacto, NO por substring, para evitar el bug
 * clásico `"mushroom".contains("room") == true`.
 */
class IngredientAnalyzer(
    private val confidenceThreshold: Float = 0.55f,
    private val onIngredientDetected: (DetectedIngredient?) -> Unit,
    private val onError: (Exception) -> Unit = {}
) {
    private val tag = "IngredientAnalyzer"
    private val isAnalyzing = AtomicBoolean(false)

    private val labeler: ImageLabeler by lazy {
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(confidenceThreshold)
            .build()
        ImageLabeling.getClient(options)
    }

    data class DetectedIngredient(
        val name: String,       // Nombre ya traducido (ej. "Tomate")
        val confidence: Float
    )

    companion object {

        // ───────────────────────────────────────────────────────────────
        // 1. TOKENS QUE NUNCA SON UN INGREDIENTE
        //    (cuerpo humano, fondo, utensilios, categorías genéricas)
        // ───────────────────────────────────────────────────────────────
        private val NON_INGREDIENT_TOKENS = setOf(
            // Cuerpo / personas
            "person", "human", "people", "man", "woman", "child", "face",
            "hand", "finger", "nail", "skin", "arm", "thumb", "gesture", "smile",
            // Entorno / mobiliario
            "poster", "wall", "room", "table", "furniture", "flooring", "wood",
            "shelf", "appliance", "paper", "textile", "cloth",
            // Utensilios de cocina
            "spoon", "fork", "knife", "plate", "bowl", "cup", "glass",
            "bottle", "jar", "pan", "pot", "kitchenware", "utensil", "cutlery",
            // Categorías genéricas de comida (esto es clave para "Food")
            "food", "fruit", "vegetable", "produce", "meal", "dish", "cuisine",
            "snack", "dessert", "baked", "goods", "recipe", "staple",
            "comfort", "natural", "whole", "local", "fried", "fast",
            "breakfast", "lunch", "dinner", "brunch", "appetizer", "entree"
        )

        // ───────────────────────────────────────────────────────────────
        // 2. INGREDIENTES ESPECÍFICOS QUE SÍ QUEREMOS ACEPTAR
        // ───────────────────────────────────────────────────────────────
        private val INGREDIENT_TOKENS = setOf(
            // Vegetales
            "tomato", "onion", "garlic", "potato", "carrot", "pepper", "chili",
            "lettuce", "spinach", "broccoli", "cabbage", "cauliflower", "zucchini",
            "cucumber", "pumpkin", "mushroom", "celery", "asparagus", "beet",
            "radish", "eggplant", "leek", "shallot", "squash", "pea", "corn",
            "kale", "arugula", "chive", "fennel", "scallion",
            // Frutas
            "apple", "banana", "orange", "lemon", "lime", "strawberry",
            "blueberry", "raspberry", "cherry", "grape", "peach", "pear",
            "plum", "mango", "pineapple", "watermelon", "melon", "coconut",
            "avocado", "kiwi", "papaya", "fig", "date", "pomegranate", "grapefruit",
            // Lácteos y huevos
            "cheese", "milk", "butter", "egg", "yogurt", "yoghurt", "cream",
            // Carnes y pescados
            "meat", "beef", "pork", "chicken", "fish", "salmon", "tuna",
            "shrimp", "bacon", "ham", "sausage", "turkey", "lamb", "duck",
            "crab", "lobster",
            // Granos, legumbres, harinas
            "bread", "pasta", "rice", "flour", "bean", "lentil", "chickpea",
            "oat", "wheat", "barley", "quinoa", "noodle", "cereal",
            // Frutos secos y semillas
            "nut", "almond", "walnut", "peanut", "cashew", "pistachio",
            "hazelnut", "seed",
            // Condimentos, especias, endulzantes
            "herb", "spice", "oil", "vinegar", "sauce", "salt", "sugar",
            "honey", "syrup", "cinnamon", "paprika", "basil", "parsley",
            "cilantro", "mint", "ginger", "turmeric", "cumin", "oregano",
            "thyme", "rosemary", "chocolate", "cocoa", "vanilla",
            // Bebidas usadas como ingredientes
            "coffee", "tea", "juice", "wine", "beer",
            // Otros
            "tofu", "tempeh", "seitan", "seaweed", "olive", "pickle", "hummus"
        )

        // ───────────────────────────────────────────────────────────────
        // 3. TRADUCCIONES AL ESPAÑOL (para mostrar en la UI)
        // ───────────────────────────────────────────────────────────────
        private val SPANISH_NAMES = mapOf(
            "tomato" to "Tomate", "onion" to "Cebolla", "garlic" to "Ajo",
            "potato" to "Papa", "carrot" to "Zanahoria", "pepper" to "Pimiento",
            "chili" to "Chile", "lettuce" to "Lechuga", "spinach" to "Espinaca",
            "broccoli" to "Brócoli", "cabbage" to "Repollo", "cauliflower" to "Coliflor",
            "zucchini" to "Calabacín", "cucumber" to "Pepino", "pumpkin" to "Calabaza",
            "mushroom" to "Champiñón", "celery" to "Apio",
            "apple" to "Manzana", "banana" to "Plátano", "orange" to "Naranja",
            "lemon" to "Limón", "lime" to "Lima", "strawberry" to "Fresa",
            "blueberry" to "Arándano", "raspberry" to "Frambuesa", "cherry" to "Cereza",
            "grape" to "Uva", "peach" to "Durazno", "pear" to "Pera", "plum" to "Ciruela",
            "mango" to "Mango", "pineapple" to "Piña", "watermelon" to "Sandía",
            "melon" to "Melón", "coconut" to "Coco", "avocado" to "Aguacate",
            "cheese" to "Queso", "milk" to "Leche", "butter" to "Mantequilla",
            "egg" to "Huevo", "yogurt" to "Yogur", "cream" to "Crema",
            "meat" to "Carne", "beef" to "Res", "pork" to "Cerdo", "chicken" to "Pollo",
            "fish" to "Pescado", "salmon" to "Salmón", "tuna" to "Atún",
            "shrimp" to "Camarón", "bacon" to "Tocino", "ham" to "Jamón",
            "sausage" to "Salchicha",
            "bread" to "Pan", "pasta" to "Pasta", "rice" to "Arroz",
            "flour" to "Harina", "bean" to "Frijol", "lentil" to "Lenteja",
            "chickpea" to "Garbanzo", "oat" to "Avena",
            "almond" to "Almendra", "walnut" to "Nuez", "peanut" to "Maní",
            "herb" to "Hierba", "spice" to "Especia", "oil" to "Aceite",
            "vinegar" to "Vinagre", "sauce" to "Salsa", "salt" to "Sal",
            "sugar" to "Azúcar", "honey" to "Miel", "cinnamon" to "Canela",
            "paprika" to "Pimentón", "basil" to "Albahaca", "parsley" to "Perejil",
            "cilantro" to "Cilantro", "ginger" to "Jengibre",
            "chocolate" to "Chocolate", "vanilla" to "Vainilla",
            "coffee" to "Café", "tea" to "Té", "juice" to "Jugo",
            "tofu" to "Tofu", "olive" to "Aceituna"
        )

        private fun normalize(label: String): String =
            label.lowercase().trim().replace(Regex("\\s+"), " ")

        /**
         * Determina si una etiqueta de ML Kit corresponde a un ingrediente válido.
         * Público porque lo consume CameraXScannerView.
         */
        fun isFoodIngredient(label: String): Boolean {
            val tokens = normalize(label).split(" ").filter { it.isNotBlank() }
            if (tokens.isEmpty()) return false
            if (tokens.any { it in NON_INGREDIENT_TOKENS }) return false
            return tokens.any { it in INGREDIENT_TOKENS }
        }

        /**
         * Traduce una etiqueta en inglés al español.
         * Recorre los tokens de atrás hacia adelante (en inglés compuesto el
         * sustantivo cabecera va al final: "Cherry tomato" → "Tomate").
         */
        fun toSpanish(englishLabel: String): String {
            val tokens = normalize(englishLabel).split(" ").reversed()
            for (token in tokens) {
                SPANISH_NAMES[token]?.let { return it }
            }
            return englishLabel.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
        }
    }

    fun processFrame(frame: Frame, rotationDegrees: Int) {
        if (!isAnalyzing.compareAndSet(false, true)) return

        var cpuImage: Image? = null
        try {
            cpuImage = frame.acquireCameraImage()
            if (cpuImage == null) {
                isAnalyzing.set(false)
                return
            }

            val inputImage = InputImage.fromMediaImage(cpuImage, rotationDegrees)

            labeler.process(inputImage)
                .addOnSuccessListener { labels ->
                    val topIngredient = labels
                        .filter { isFoodIngredient(it.text) }
                        .maxByOrNull { it.confidence }
                        ?.let {
                            DetectedIngredient(
                                name = toSpanish(it.text),
                                confidence = it.confidence
                            )
                        }
                    onIngredientDetected(topIngredient)
                }
                .addOnFailureListener { exception ->
                    Log.e(tag, "Error en el análisis de ML Kit", exception)
                    onError(exception)
                }
                .addOnCompleteListener {
                    cpuImage.close()
                    isAnalyzing.set(false)
                }
        } catch (e: Exception) {
            Log.e(tag, "Excepción al procesar fotograma", e)
            cpuImage?.close()
            isAnalyzing.set(false)
        }
    }

    fun close() {
        labeler.close()
    }
}