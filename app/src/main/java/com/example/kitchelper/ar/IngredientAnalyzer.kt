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
 * Estrategia de filtrado optimizada (Umbral 0.45f para mayor sensibilidad):
 *  1. Tokenizar la etiqueta devuelta por ML Kit (separada por espacios).
 *  2. Rechazar si CUALQUIER token está en la blacklist.
 *  3. Aceptar si ALGÚN token está en la whitelist de ingredientes específicos.
 */
class IngredientAnalyzer(
    private val confidenceThreshold: Float = 0.45f,
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
        val name: String,       // Nombre traducido
        val confidence: Float
    )

    companion object {
        private val NON_INGREDIENT_TOKENS = setOf(
            "person", "human", "people", "man", "woman", "child", "face",
            "hand", "finger", "nail", "skin", "arm", "thumb", "gesture", "smile",
            "poster", "wall", "room", "table", "furniture", "flooring", "wood",
            "shelf", "appliance", "paper", "textile", "cloth",
            "spoon", "fork", "knife", "plate", "bowl", "cup", "glass",
            "bottle", "jar", "pan", "pot", "kitchenware", "utensil", "cutlery",
            "food", "fruit", "vegetable", "produce", "meal", "dish", "cuisine",
            "snack", "dessert", "baked", "goods", "recipe", "staple",
            "comfort", "natural", "whole", "local", "fried", "fast",
            "breakfast", "lunch", "dinner", "brunch", "appetizer", "entree"
        )

        private val INGREDIENT_TOKENS = setOf(
            "tomato", "onion", "garlic", "potato", "carrot", "pepper", "chili",
            "lettuce", "spinach", "broccoli", "cabbage", "cauliflower", "zucchini",
            "cucumber", "pumpkin", "mushroom", "celery", "asparagus", "beet",
            "radish", "eggplant", "leek", "shallot", "squash", "pea", "corn",
            "kale", "arugula", "chive", "fennel", "scallion",
            "apple", "banana", "orange", "lemon", "lime", "strawberry",
            "blueberry", "raspberry", "cherry", "grape", "peach", "pear",
            "plum", "mango", "pineapple", "watermelon", "melon", "coconut",
            "avocado", "kiwi", "papaya", "fig", "date", "pomegranate", "grapefruit",
            "cheese", "milk", "butter", "egg", "yogurt", "yoghurt", "cream",
            "meat", "beef", "pork", "chicken", "fish", "salmon", "tuna",
            "shrimp", "bacon", "ham", "sausage", "turkey", "lamb", "duck",
            "crab", "lobster",
            "bread", "pasta", "rice", "flour", "bean", "lentil", "chickpea",
            "oat", "wheat", "barley", "quinoa", "noodle", "cereal",
            "nut", "almond", "walnut", "peanut", "cashew", "pistachio",
            "hazelnut", "seed",
            "herb", "spice", "oil", "vinegar", "sauce", "salt", "sugar",
            "honey", "syrup", "cinnamon", "paprika", "basil", "parsley",
            "cilantro", "mint", "ginger", "turmeric", "cumin", "oregano",
            "thyme", "rosemary", "chocolate", "cocoa", "vanilla",
            "coffee", "tea", "juice", "wine", "beer",
            "tofu", "tempeh", "seitan", "seaweed", "olive", "pickle", "hummus"
        )

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

        fun isFoodIngredient(label: String): Boolean {
            val tokens = normalize(label).split(" ").filter { it.isNotBlank() }
            if (tokens.isEmpty()) return false
            if (tokens.any { it in NON_INGREDIENT_TOKENS }) return false
            return tokens.any { it in INGREDIENT_TOKENS }
        }

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
