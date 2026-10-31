package com.example.kitchelper.data.repository

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.example.kitchelper.data.model.Paso
import com.example.kitchelper.data.model.Receta
import com.example.kitchelper.data.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "KitchelperGemini"

class GeminiAiRepository {

    // Clave API de Gemini cargada desde local.properties mediante BuildConfig
    private var apiKey: String = try {
        val key: String = com.example.kitchelper.BuildConfig.GEMINI_API_KEY
        if (key.isNotBlank()) key else ""
    } catch (_: Throwable) {
        ""
    }

    private fun obtenerModelos(): List<GenerativeModel> {
        return listOf(
            GenerativeModel(modelName = "gemini-3.8-flash", apiKey = apiKey),
            GenerativeModel(modelName = "gemini-3.5-flash-lite", apiKey = apiKey)
        )
    }


    private suspend fun ejecutarConsultaGemini(prompt: String): String? {
        if (apiKey.isBlank()) {
            return null
        }

        for (model in obtenerModelos()) {
            try {
                val response = model.generateContent(prompt)
                val text = response.text
                if (!text.isNullOrBlank()) {
                    Log.d(TAG, "✅ [GEMINI] Respuesta generada exitosamente con modelo '${model.modelName}':\n$text")
                    return text.trim()
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.w(TAG, "⚠️ Intento con modelo '${model.modelName}' falló (${e.javaClass.simpleName}: ${e.message}). Probando siguiente...")
            }
        }
        return null
    }

    fun setApiKey(newKey: String) {
        if (newKey.isNotBlank()) {
            apiKey = newKey
            Log.d(TAG, "🔑 Nueva API Key configurada en GeminiAiRepository.")
        }
    }

    /**
     * Generador dinámico local de información de ingrediente
     */
    private fun generarInfoIngredienteLocal(nombreIngrediente: String, ubicacionUsuario: String): String {
        val ingLower = nombreIngrediente.lowercase()
        val (precioStr, tiendaStr, tipStr) = when {
            ingLower.contains("carne") || ingLower.contains("res") || ingLower.contains("pollo") || ingLower.contains("cerdo") || ingLower.contains("pescado") ->
                Triple("${'$'}12.000 - ${'$'}22.000 COP la libra", "Famas locales, Éxito, Carulla, Olímpica, D1 o Zenu", "Elige cortes de color rojo brillante o rosado fresco con marmoleado uniforme de grasa blanca.")
            ingLower.contains("leche") || ingLower.contains("queso") || ingLower.contains("crema") || ingLower.contains("mantequilla") || ingLower.contains("yogur") ->
                Triple("${'$'}3.500 - ${'$'}8.500 COP la unidad", "Tiendas D1, Ara, Carulla, Éxito o supermercados de barrio", "Verifica la fecha de vencimiento y mantenlo refrigerado entre 2°C y 4°C en todo momento.")
            ingLower.contains("tomate") || ingLower.contains("cebolla") || ingLower.contains("aguacate") || ingLower.contains("papa") || ingLower.contains("zanahoria") || ingLower.contains("limón") || ingLower.contains("limon") ->
                Triple("${'$'}2.000 - ${'$'}5.500 COP la libra", "Plazas de mercado (Paloquemao / La Mayorista), fruterías locales o Éxito/Carulla", "Busca ejemplares de consistencia firme, piel limpia y sin magulladuras ni manchas oscuras.")
            ingLower.contains("harina") || ingLower.contains("azúcar") || ingLower.contains("azucar") || ingLower.contains("arroz") || ingLower.contains("aceite") || ingLower.contains("sal") ->
                Triple("${'$'}3.200 - ${'$'}7.000 COP la presentación", "Tiendas Ara, D1, Éxito, Olímpica y tiendas de víveres", "Consérvalo en un recipiente hermético en un lugar fresco y seco alejado de la humedad directa.")
            else ->
                Triple("${'$'}3.000 - ${'$'}8.500 COP la porción", "Supermercados como Éxito, Carulla, D1, Ara y plazas de mercado en $ubicacionUsuario", "Verifica su textura, aroma característico de frescura y empaque sellado.")
        }

        return "🛒 Dónde comprar $nombreIngrediente en $ubicacionUsuario:\n$tiendaStr.\n\n" +
               "💰 Precio estimado:\n$precioStr.\n\n" +
               "💡 Consejo útil:\n$tipStr"
    }

    /**
     * Generador dinámico local de consejos por paso
     */
    private fun generarConsejoPasoLocal(tituloReceta: String, pasoActual: Paso, usuario: Usuario): String {
        val nivel = usuario.informacionCulinaria.experticia.ifBlank { "Principiante" }
        val descOriginal = pasoActual.descripcion.trim().removeSuffix(".")
        val descLower = descOriginal.lowercase()
        val numPaso = pasoActual.orden

        return buildString {
            append("👨‍🍳 Consejo para '$tituloReceta' - Paso $numPaso ($nivel):\n")

            when {
                descLower.contains("corta") || descLower.contains("pica") || descLower.contains("reban") || descLower.contains("troce") || descLower.contains("pel") -> {
                    if (nivel.equals("Principiante", ignoreCase = true)) {
                        append("Para '$descOriginal': asegura tu tabla con un paño húmedo debajo y utiliza el 'agarre en garra' doblando las yemas de tus dedos hacia adentro para cortar sin riesgo.")
                    } else {
                        append("Para '$descOriginal': mantén cortes parejos e iguales para garantizar una cocción 100% uniforme en la preparación.")
                    }
                }
                descLower.contains("sal") || descLower.contains("sazona") || descLower.contains("condiment") || descLower.contains("pimienta") || descLower.contains("especi") -> {
                    append("Al '$descOriginal': incorpora los condimentos gradualmente y prueba la preparación para balancear los sabores.")
                }
                descLower.contains("aceite") || descLower.contains("freír") || descLower.contains("fría") || descLower.contains("fuego") || descLower.contains("sartén") || descLower.contains("sarten") || descLower.contains("sofre") || descLower.contains("dora") -> {
                    append("Para '$descOriginal': precalienta la sartén 1 minuto a fuego medio antes de poner la grasa para lograr un dorado uniforme.")
                }
                descLower.contains("azúcar") || descLower.contains("azucar") || descLower.contains("endulza") || descLower.contains("miel") -> {
                    append("Al '$descOriginal': disuelve el endulzante a fuego medio-bajo para integrarlo bien sin quemarlo.")
                }
                descLower.contains("mezcla") || descLower.contains("bat") || descLower.contains("incorpor") || descLower.contains("remuev") || descLower.contains("revolv") -> {
                    append("Para '$descOriginal': realiza movimientos envolventes desde los bordes hacia el centro del recipiente para mantener una textura fluida e intoda.")
                }
                descLower.contains("horn") || descLower.contains("gratin") -> {
                    append("Al '$descOriginal': precalienta el horno con anticipación y ubica la bandeja en la rejilla central para una distribución homogénea del calor.")
                }
                descLower.contains("herv") || descLower.contains("cocin") || descLower.contains("olla") || descLower.contains("agua") || descLower.contains("caldo") -> {
                    append("Para '$descOriginal': mantén la olla tapada hasta que hierva para conservar los aromas y luego ajusta a fuego medio.")
                }
                else -> {
                    append("En la acción de '$descOriginal': realiza el procedimiento a fuego adecuado y supervisa de cerca los cambios de aroma y textura hasta alcanzar el punto óptimo.")
                }
            }
        }
    }

    /**
     * Obtener información sobre dónde comprar un ingrediente específico en Colombia,
     * su rango de precio estimado en pesos colombianos (COP $) y un consejo de calidad.
     */
    suspend fun obtenerInfoIngredienteConUbicacion(
        nombreIngrediente: String,
        ubicacionUsuario: String = "Colombia"
    ): Result<String> = withContext(Dispatchers.IO) {
        val prompt = """
            Eres el asistente de compras de la app KitchenPer. Un usuario en "$ubicacionUsuario" necesita comprar el ingrediente "$nombreIngrediente" para su receta.
            
            Responde con datos específicos para "$nombreIngrediente" en Colombia con este formato exacto:
            🛒 Dónde comprar: (menciona cadenas exactas como Éxito, Carulla, D1, Ara, Olímpica o plazas de mercado)
            💰 Precio estimado: (menciona un rango estimado en pesos colombianos COP $)
            💡 Consejo útil: (un consejo único de frescura o selección para $nombreIngrediente)
            
            Sé muy directo, conciso y amigable.
        """.trimIndent()

        Log.d(TAG, "=========================================================")
        Log.d(TAG, "🤖 [GEMINI INGREDIENTE] Solicitando información para: '$nombreIngrediente' (Ubicación: $ubicacionUsuario)")

        try {
            val resultadoIA = ejecutarConsultaGemini(prompt)
            if (!resultadoIA.isNullOrBlank()) {
                Result.success(resultadoIA)
            } else {
                val fallback = generarInfoIngredienteLocal(nombreIngrediente, ubicacionUsuario)
                Log.w(TAG, "ℹ️ Respuesta de API no disponible. Usando motor inteligente local:\n$fallback")
                Result.success(fallback)
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            val fallback = generarInfoIngredienteLocal(nombreIngrediente, ubicacionUsuario)
            Log.e(TAG, "❌ Excepción en consulta Gemini IA para '$nombreIngrediente': ${e.message}")
            Result.success(fallback)
        }
    }

    /**
     * Generar un consejo técnico y motivador adaptado EXCLUSIVAMENTE a la descripción de CADA PASO individual.
     */
    suspend fun obtenerConsejoPasoASpaso(
        tituloReceta: String,
        pasoActual: Paso,
        usuario: Usuario
    ): Result<String> = withContext(Dispatchers.IO) {
        val nivel = usuario.informacionCulinaria.experticia.ifBlank { "Principiante" }
        val descOriginal = pasoActual.descripcion.trim().removeSuffix(".")
        val numPaso = pasoActual.orden

        val prompt = """
            Eres el mentor de cocina inteligente de KitchenPer. Genera un consejo de cocina ÚNICO y sumamente personalizado para el Paso $numPaso de la receta "$tituloReceta".

            DATOS DEL PASO $numPaso DE "$tituloReceta":
            - Acción a realizar: "$descOriginal"

            PERFIL DEL USUARIO:
            - Nivel de Experticia: $nivel

            INSTRUCCIONES OBLIGATORIAS:
            1. MENCIONA EXPLÍCITAMENTE la acción "$descOriginal" y la receta "$tituloReceta".
            2. Genera un consejo de 2 frases breves, fresco y completamente diferente a otros pasos.
            3. Inicia la respuesta con 👨‍🍳.
        """.trimIndent()

        Log.d(TAG, "=========================================================")
        Log.d(TAG, "🤖 [GEMINI PASO A PASO] Solicitando consejo para Paso $numPaso de '$tituloReceta' (Acción: '$descOriginal')")

        try {
            val resultadoIA = ejecutarConsultaGemini(prompt)
            if (!resultadoIA.isNullOrBlank()) {
                Result.success(resultadoIA)
            } else {
                val fallback = generarConsejoPasoLocal(tituloReceta, pasoActual, usuario)
                Log.w(TAG, "ℹ️ Respuesta de API no disponible. Usando consejo inteligente local:\n$fallback")
                Result.success(fallback)
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            val fallback = generarConsejoPasoLocal(tituloReceta, pasoActual, usuario)
            Log.e(TAG, "❌ Excepción en consulta Gemini IA para Paso $numPaso: ${e.message}")
            Result.success(fallback)
        }
    }

    /**
     * Generar adaptaciones y consejos de sustitución de ingredientes personalizados para la receta completa.
     */
    suspend fun generarSugerenciasPersonalizadasReceta(
        usuario: Usuario,
        receta: Receta
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        val culinaria = usuario.informacionCulinaria
        val salud = usuario.informacionSalud

        val alergiasStr = salud.alergias.filter { it.isNotBlank() }.joinToString(", ")
        val restriccionesStr = salud.restricciones.filter { it.isNotBlank() }.joinToString(", ")
        val problemasStr = salud.problemasSalud.filter { it.isNotBlank() }.joinToString(", ")
        val ingredientesStr = receta.ingredientes.joinToString(", ") { "${it.nombre} (${it.cantidad})" }

        val prompt = """
            Eres el chef inteligente de KitchenPer. Analiza la receta "${receta.titulo}" y compárala con el perfil del usuario.

            PERFIL DEL USUARIO:
            - Nivel de Experticia: ${culinaria.experticia.ifBlank { "Principiante" }}
            - Endulzante preferido: ${culinaria.endulzante.ifBlank { "Azúcar" }}
            - Tipo de alimentación: ${salud.tipoAlimentacion.ifBlank { "Normal" }}
            - Alergias declaradas: ${alergiasStr.ifBlank { "Ninguna" }}
            - Problemas de salud: ${problemasStr.ifBlank { "Ninguno" }}
            - Restricciones alimentarias: ${restriccionesStr.ifBlank { "Ninguna" }}

            INGREDIENTES DE LA RECETA:
            $ingredientesStr

            INSTRUCCIONES:
            Identifica si algún ingrediente de "${receta.titulo}" contradice las preferencias o salud del usuario.
            Devuelve de 1 a 3 recomendaciones de sustitución directas mencionando la receta. Cada sugerencia debe empezar con el ícono 💡 o ⚠️.
        """.trimIndent()

        Log.d(TAG, "=========================================================")
        Log.d(TAG, "🤖 [GEMINI ADAPTACIÓN RECETA] Analizando perfil vs receta '${receta.titulo}'")

        try {
            val resultadoIA = ejecutarConsultaGemini(prompt)
            if (!resultadoIA.isNullOrBlank()) {
                val lineas = resultadoIA.lines()
                    .map { it.trim() }
                    .filter { it.isNotBlank() && (it.startsWith("💡") || it.startsWith("⚠️") || it.startsWith("-") || it.startsWith("*")) }
                    .map { it.removePrefix("- ").removePrefix("* ") }

                if (lineas.isNotEmpty()) {
                    Result.success(lineas)
                } else {
                    Result.success(listOf("💡 Esta receta es adecuada para tu perfil culinario."))
                }
            } else {
                val fallback = generarSugerenciasLocales(usuario, receta)
                Result.success(fallback)
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "❌ Excepción en consulta Gemini IA para adaptación de receta '${receta.titulo}': ${e.message}")
            val fallback = generarSugerenciasLocales(usuario, receta)
            Result.success(fallback)
        }
    }

    private fun generarSugerenciasLocales(usuario: Usuario, receta: Receta): List<String> {
        val culinaria = usuario.informacionCulinaria
        val salud = usuario.informacionSalud
        val consejosLocales = mutableListOf<String>()

        val endulzante = culinaria.endulzante
        if (endulzante.isNotBlank() && !endulzante.equals("Azúcar", ignoreCase = true) &&
            receta.ingredientes.any { it.nombre.contains("azúcar", ignoreCase = true) || it.nombre.contains("azucar", ignoreCase = true) }) {
            consejosLocales.add("💡 En '${receta.titulo}': Tu perfil indica preferencia por $endulzante. Puedes sustituir el azúcar tradicional por $endulzante al gusto.")
        }

        if (salud.tipoAlimentacion.equals("Fitness", ignoreCase = true)) {
            consejosLocales.add("💡 Para '${receta.titulo}': Para mantener tu perfil Fitness, te recomendamos usar freidora de aire o aceite de oliva en aerosol.")
        }

        if (salud.alergias.any { it.contains("lactosa", ignoreCase = true) || it.contains("lácteos", ignoreCase = true) } &&
            receta.ingredientes.any { it.nombre.contains("leche", ignoreCase = true) || it.nombre.contains("queso", ignoreCase = true) || it.nombre.contains("crema", ignoreCase = true) }) {
            consejosLocales.add("⚠️ En '${receta.titulo}': Notamos alergia/intolerancia a lácteos en tu perfil. Te sugerimos sustituir lácteos por versiones deslactosada o vegetal.")
        }

        if (consejosLocales.isEmpty()) {
            consejosLocales.add("💡 La receta '${receta.titulo}' se ajusta perfectamente a tu perfil culinario de nivel ${culinaria.experticia.ifBlank { "Principiante" }}.")
        }

        return consejosLocales
    }
}
