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
            GenerativeModel(modelName = "gemini-2.0-flash", apiKey = apiKey),
            GenerativeModel(modelName = "gemini-1.5-flash", apiKey = apiKey),
            GenerativeModel(modelName = "gemini-1.5-flash-8b", apiKey = apiKey)
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
     * Generador dinámico local de consejos por paso adaptados al perfil del usuario
     */
    private fun generarConsejoPasoLocal(tituloReceta: String, pasoActual: Paso, usuario: Usuario): String {
        val culinaria = usuario.informacionCulinaria
        val salud = usuario.informacionSalud
        val nivel = culinaria.experticia.ifBlank { "Principiante" }
        val descOriginal = pasoActual.descripcion.trim().removeSuffix(".")
        val descLower = descOriginal.lowercase()
        val numPaso = pasoActual.orden

        val tieneAlergiaLacteos = salud.alergias.any { it.contains("lactosa", ignoreCase = true) || it.contains("lácteos", ignoreCase = true) || it.contains("leche", ignoreCase = true) }
        val tieneAlergiaGluten = salud.alergias.any { it.contains("gluten", ignoreCase = true) || it.contains("trigo", ignoreCase = true) }
        val tieneHipertension = salud.problemasSalud.any { it.contains("hipertensión", ignoreCase = true) || it.contains("presión", ignoreCase = true) || it.contains("tensión", ignoreCase = true) }
        val tieneDiabetes = salud.problemasSalud.any { it.contains("diabetes", ignoreCase = true) || it.contains("glucosa", ignoreCase = true) || it.contains("azúcar", ignoreCase = true) }
        val esFitness = salud.tipoAlimentacion.equals("Fitness", ignoreCase = true)

        return buildString {
            when {
                (descLower.contains("sal") || descLower.contains("sazona") || descLower.contains("condiment")) && tieneHipertension -> {
                    append("⚠️ Por tu salud (control de tensión/sodio) en Paso $numPaso: reduce o evita añadir sal al '$descOriginal'. Usa especias naturales como ajo, orégano o pimienta.")
                }
                (descLower.contains("azúcar") || descLower.contains("azucar") || descLower.contains("endulza")) && (tieneDiabetes || (culinaria.endulzante.isNotBlank() && !culinaria.endulzante.equals("Azúcar", ignoreCase = true))) -> {
                    val endulzanteUso = if (culinaria.endulzante.isNotBlank() && !culinaria.endulzante.equals("Azúcar", ignoreCase = true)) culinaria.endulzante else "estevia o eritritol"
                    append("💡 En Paso $numPaso ($descOriginal): según tu perfil, sustituye el azúcar por $endulzanteUso para mantener tus niveles adecuados.")
                }
                (descLower.contains("leche") || descLower.contains("queso") || descLower.contains("crema") || descLower.contains("mantequilla")) && tieneAlergiaLacteos -> {
                    append("⚠️ Alerta al '$descOriginal': Recuerda utilizar opciones deslactosadas o bebida vegetal para evitar alérgenos de tu perfil.")
                }
                (descLower.contains("harina") || descLower.contains("pan") || descLower.contains("trigo")) && tieneAlergiaGluten -> {
                    append("⚠️ Alerta al '$descOriginal': Utiliza harina de avena certificada, almendras o maíz sin gluten según tu registro.")
                }
                (descLower.contains("aceite") || descLower.contains("freír") || descLower.contains("fría") || descLower.contains("sartén")) && esFitness -> {
                    append("💡 Para tu alimentación Fitness al '$descOriginal': usa la freidora de aire o aceite en aerosol a fuego controlado.")
                }
                descLower.contains("corta") || descLower.contains("pica") || descLower.contains("reban") || descLower.contains("troce") || descLower.contains("pel") -> {
                    if (nivel.equals("Principiante", ignoreCase = true)) {
                        append("👨‍🍳 Consejo ($nivel) para '$descOriginal': coloca un paño húmedo bajo tu tabla y usa el agarre en garra con los dedos hacia adentro para cortar con seguridad.")
                    } else {
                        append("👨‍🍳 Consejo ($nivel) para '$descOriginal': realiza cortes uniformes para garantizar una cocción perfecta en cada pieza.")
                    }
                }
                descLower.contains("sal") || descLower.contains("sazona") || descLower.contains("condiment") || descLower.contains("pimienta") || descLower.contains("especi") -> {
                    append("👨‍🍳 Al '$descOriginal': sazona gradualmente y prueba la preparación para equilibrar los sabores según tu gusto (${culinaria.nivelSalado.ifBlank { "equilibrado" }}).")
                }
                descLower.contains("aceite") || descLower.contains("freír") || descLower.contains("fría") || descLower.contains("fuego") || descLower.contains("sartén") || descLower.contains("sarten") || descLower.contains("sofre") || descLower.contains("dora") -> {
                    append("👨‍🍳 Para '$descOriginal': precalienta el recipiente 1 minuto antes de agregar la materia grasa para sellar los sabores.")
                }
                descLower.contains("mezcla") || descLower.contains("bat") || descLower.contains("incorpor") || descLower.contains("remuev") || descLower.contains("revolv") -> {
                    append("👨‍🍳 Para '$descOriginal': integra con movimientos suaves de afuera hacia adentro para conservar la textura óptima.")
                }
                descLower.contains("horn") || descLower.contains("gratin") -> {
                    append("👨‍🍳 Al '$descOriginal': asegúrate de precalentar el horno y usar la rejilla central para una cocción homogénea.")
                }
                else -> {
                    append("👨‍🍳 Consejo ($nivel) para '$descOriginal' en '$tituloReceta': supervisa la textura y temperatura para asegurar un resultado acorde a tus preferencias.")
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
     * Generar un consejo técnico y motivador adaptado EXCLUSIVAMENTE a la descripción de CADA PASO individual y al PERFIL COMPLETO del usuario.
     */
    suspend fun obtenerConsejoPasoASpaso(
        tituloReceta: String,
        pasoActual: Paso,
        usuario: Usuario
    ): Result<String> = withContext(Dispatchers.IO) {
        val culinaria = usuario.informacionCulinaria
        val salud = usuario.informacionSalud

        val nivel = culinaria.experticia.ifBlank { "Principiante" }
        val tipoAlimentacion = salud.tipoAlimentacion.ifBlank { "Normal" }
        val endulzante = culinaria.endulzante.ifBlank { "Azúcar" }
        val alergiasStr = salud.alergias.filter { it.isNotBlank() }.joinToString(", ")
        val problemasStr = salud.problemasSalud.filter { it.isNotBlank() }.joinToString(", ")
        val restriccionesStr = salud.restricciones.filter { it.isNotBlank() }.joinToString(", ")

        val descOriginal = pasoActual.descripcion.trim().removeSuffix(".")
        val numPaso = pasoActual.orden

        val prompt = """
            Eres el mentor de cocina inteligente de KitchenPer en Realidad Aumentada y Guía en Vivo. Genera un consejo de cocina ÚNICO y altamente personalizado para el Paso $numPaso de la receta "$tituloReceta".

            DATOS DEL PASO $numPaso DE "$tituloReceta":
            - Acción a realizar: "$descOriginal"

            PERFIL DEL USUARIO:
            - Nivel de Experticia: $nivel
            - Tipo de Alimentación: $tipoAlimentacion
            - Endulzante preferido: $endulzante
            - Alergias declaradas: ${alergiasStr.ifBlank { "Ninguna" }}
            - Afecciones / Salud: ${problemasStr.ifBlank { "Ninguno" }}
            - Restricciones alimentarias: ${restriccionesStr.ifBlank { "Ninguna" }}
            - Preferencias de sabor: Salado (${culinaria.nivelSalado.ifBlank { "Normal" }}), Dulce (${culinaria.nivelDulce.ifBlank { "Normal" }}), Picante (${culinaria.picante.ifBlank { "No" }})

            INSTRUCCIONES OBLIGATORIAS:
            1. MENCIONA EXPLÍCITAMENTE la acción "$descOriginal" y la receta "$tituloReceta".
            2. Si la acción o ingredientes del paso chocan con las alergias, salud (ej: hipertensión -> menos sal, diabetes -> usar $endulzante) o alimentación del usuario, ALERTA o sugiere la alternativa directamente.
            3. Si es principiante, ofrece un tip técnico de seguridad o precisión. Si es avanzado/intermedio, sugiere un tip de técnica profesional.
            4. Responde con 2 frases breves y directas.
            5. Inicia la respuesta con 👨‍🍳 (o ⚠️ si es una advertencia de salud/alérgeno).
        """.trimIndent()

        Log.d(TAG, "=========================================================")
        Log.d(TAG, "🤖 [GEMINI PASO A PASO] Solicitando consejo personalizado para Paso $numPaso de '$tituloReceta' (Perfil: $nivel, Salud: $problemasStr, Alergias: $alergiasStr)")

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
        val pasosStr = receta.pasos.take(4).joinToString("; ") { "Paso ${it.orden}: ${it.descripcion}" }

        val prompt = """
            Eres el chef mentor inteligente de KitchenPer. Tu función es comparar minuciosamente la receta "${receta.titulo}" con el perfil del usuario y darle de 2 a 4 recomendaciones directas de adaptación o sustitución.

            PERFIL COMPLETO DEL USUARIO:
            - Nivel de Experticia: ${culinaria.experticia.ifBlank { "Principiante" }}
            - Tipo de Alimentación: ${salud.tipoAlimentacion.ifBlank { "Normal" }}
            - Endulzante preferido: ${culinaria.endulzante.ifBlank { "Azúcar" }}
            - Nivel de Salado / Sabor: ${culinaria.nivelSalado.ifBlank { "Equilibrado" }} (Dulce: ${culinaria.nivelDulce.ifBlank { "Normal" }}, Picante: ${culinaria.picante.ifBlank { "No" }})
            - Condimento preferido: ${culinaria.condimento.ifBlank { "Natural" }}
            - Alergias declaradas: ${alergiasStr.ifBlank { "Ninguna" }}
            - Afecciones / Problemas de Salud: ${problemasStr.ifBlank { "Ninguno" }}
            - Restricciones alimentarias: ${restriccionesStr.ifBlank { "Ninguna" }}

            DATOS DE LA RECETA "${receta.titulo}":
            - Ingredientes: $ingredientesStr
            - Pasos de preparación: $pasosStr

            INSTRUCCIONES OBLIGATORIAS:
            1. Compara cada ingrediente y paso de la receta con las alergias, problemas de salud, tipo de alimentación y gustos del usuario.
            2. Genera entre 2 y 4 recomendaciones o alertas hiper-personalizadas.
            3. Si hay alergia o condición de salud (ej: hipertensión, diabetes, lactosa, gluten), incluye una advertencia que inicie con ⚠️.
            4. Si hay sugerencia de sabor, técnica o sustitución conveniente (ej: endulzante preferido, técnica fitness, o consejo según su nivel ${culinaria.experticia}), incluye una viñeta que inicie con 💡.
            5. Responde ÚNICAMENTE con los puntos de recomendación. Cada punto en una nueva línea.
        """.trimIndent()

        Log.d(TAG, "=========================================================")
        Log.d(TAG, "🤖 [GEMINI ADAPTACIÓN RECETA] Analizando perfil vs receta '${receta.titulo}'")

        try {
            val resultadoIA = ejecutarConsultaGemini(prompt)
            if (!resultadoIA.isNullOrBlank()) {
                val lineasLimpias = resultadoIA.lines()
                    .map { line ->
                        line.trim()
                            .replace(Regex("^(?:\\d+\\.|[\\-*•]|\\*\\*\\d+\\.\\*\\*|\\*\\*)\\s*"), "")
                            .replace(Regex("^\\*\\*|\\*\\*$"), "")
                            .trim()
                    }
                    .filter { it.isNotBlank() && it.length > 6 }
                    .map { line ->
                        when {
                            line.startsWith("💡") || line.startsWith("⚠️") -> line
                            line.lowercase().contains("alerta") || line.lowercase().contains("riesgo") ||
                            line.lowercase().contains("alergia") || line.lowercase().contains("evita") ||
                            line.lowercase().contains("precaución") || line.lowercase().contains("intoleran") -> "⚠️ $line"
                            else -> "💡 $line"
                        }
                    }

                if (lineasLimpias.isNotEmpty()) {
                    Result.success(lineasLimpias)
                } else {
                    val fallback = generarSugerenciasLocales(usuario, receta)
                    Result.success(fallback)
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

        val titulo = receta.titulo
        val ingList = receta.ingredientes

        val tieneHipertension = salud.problemasSalud.any { it.contains("hipertensión", ignoreCase = true) || it.contains("presión", ignoreCase = true) || it.contains("tensión", ignoreCase = true) || it.contains("sodio", ignoreCase = true) }
        val tieneDiabetes = salud.problemasSalud.any { it.contains("diabetes", ignoreCase = true) || it.contains("glucosa", ignoreCase = true) || it.contains("azúcar", ignoreCase = true) }
        val esFitness = salud.tipoAlimentacion.equals("Fitness", ignoreCase = true)
        val esVeganoOVegetariano = salud.tipoAlimentacion.contains("Vegan", ignoreCase = true) || salud.tipoAlimentacion.contains("Vegetar", ignoreCase = true)
        val endulzanteUso = if (culinaria.endulzante.isNotBlank() && !culinaria.endulzante.equals("Azúcar", ignoreCase = true)) culinaria.endulzante else "estevia o eritritol"

        // 1. Sal / Sodio -> SOLAMENTE si el usuario tiene Hipertensión Y la receta contiene sal
        val ingSal = ingList.find { it.nombre.lowercase().contains("sal") }
        if (tieneHipertension && ingSal != null) {
            consejosLocales.add("⚠️ Por tu perfil de hipertensión/sodio: reemplaza ${ingSal.cantidad} de ${ingSal.nombre} en $titulo por especias naturales (ajo, comino, orégano).")
        }

        // 2. Ketchup / Salsas Procesadas -> SOLAMENTE si la receta REALMENTE contiene ketchup o salsa procesada
        val ingKetchup = ingList.find { it.nombre.lowercase().contains("ketchup") || it.nombre.lowercase().contains("salsa de tomate") || it.nombre.lowercase().contains("mayonesa") }
        if (ingKetchup != null && (tieneHipertension || esFitness || tieneDiabetes)) {
            consejosLocales.add("💡 Para $titulo: sustituye ${ingKetchup.cantidad} de ${ingKetchup.nombre} por tomate natural triturado para reducir sodio y azúcares procesados.")
        }

        // 3. Arroz / Carbohidratos -> SOLAMENTE si la receta contiene arroz/pasta/papa Y el usuario es Fitness
        val ingCarbo = ingList.find { it.nombre.lowercase().contains("arroz") || it.nombre.lowercase().contains("papa") || it.nombre.lowercase().contains("pasta") }
        if (ingCarbo != null && esFitness) {
            consejosLocales.add("💡 Para tu perfil Fitness en $titulo: cambia ${ingCarbo.cantidad} de ${ingCarbo.nombre} por versión integral o aumenta la porción de vegetales.")
        }

        // 4. Lácteos -> SOLAMENTE si el usuario tiene Alergia/Intolerancia a Lácteos Y la receta contiene lácteos
        val ingLacteos = ingList.find { it.nombre.lowercase().contains("leche") || it.nombre.lowercase().contains("queso") || it.nombre.lowercase().contains("crema") || it.nombre.lowercase().contains("mantequilla") || it.nombre.lowercase().contains("yogur") }
        val tieneLacteosAlergia = salud.alergias.any { it.contains("lactosa", ignoreCase = true) || it.contains("lácteos", ignoreCase = true) || it.contains("leche", ignoreCase = true) }
        if (tieneLacteosAlergia && ingLacteos != null) {
            consejosLocales.add("⚠️ Alerta por tu intolerancia/alergia a lácteos: reemplaza ${ingLacteos.cantidad} de ${ingLacteos.nombre} en $titulo por bebidas/versiones vegetales o sin lactosa.")
        }

        // 5. Gluten / Trigo -> SOLAMENTE si el usuario tiene Alergia a Gluten Y la receta contiene gluten/harina
        val ingGluten = ingList.find { it.nombre.lowercase().contains("harina") || it.nombre.lowercase().contains("pan") || it.nombre.lowercase().contains("trigo") }
        val tieneGlutenAlergia = salud.alergias.any { it.contains("gluten", ignoreCase = true) || it.contains("trigo", ignoreCase = true) }
        if (tieneGlutenAlergia && ingGluten != null) {
            consejosLocales.add("⚠️ Alerta por tu intolerancia al gluten: sustituye ${ingGluten.cantidad} de ${ingGluten.nombre} en $titulo por harina de avena sin gluten o de almendras.")
        }

        // 6. Proteína Animal en Dieta Vegana/Vegetariana -> SOLAMENTE si usuario es Vegano/Vegetariano Y receta contiene carne/pollo/pescado
        val ingCarne = ingList.find { it.nombre.lowercase().contains("pollo") || it.nombre.lowercase().contains("carne") || it.nombre.lowercase().contains("cerdo") || it.nombre.lowercase().contains("pescado") }
        if (esVeganoOVegetariano && ingCarne != null) {
            consejosLocales.add("💡 Para tu alimentación ${salud.tipoAlimentacion}: reemplaza ${ingCarne.cantidad} de ${ingCarne.nombre} en $titulo por tofu, seitán o legumbres.")
        }

        // 7. Azúcar -> SOLAMENTE si la receta contiene Azúcar/Panela/Miel Y el usuario es Diabético o prefiere Endulzante
        val ingAzucar = ingList.find { it.nombre.lowercase().contains("azúcar") || it.nombre.lowercase().contains("azucar") || it.nombre.lowercase().contains("panela") || it.nombre.lowercase().contains("miel") }
        if (ingAzucar != null && (tieneDiabetes || (!culinaria.endulzante.equals("Azúcar", ignoreCase = true) && culinaria.endulzante.isNotBlank()))) {
            consejosLocales.add("💡 Sustituye ${ingAzucar.cantidad} de ${ingAzucar.nombre} en $titulo por $endulzanteUso según tus preferencias de salud.")
        }

        // 8. Recomendación por Nivel de Experticia o Compatibilidad General si no hay conflictos
        val experticia = culinaria.experticia.ifBlank { "Principiante" }
        if (consejosLocales.isEmpty()) {
            when {
                experticia.equals("Principiante", ignoreCase = true) -> {
                    consejosLocales.add("💡 La receta '$titulo' se ajusta a tu perfil. Tip para principiantes: mide todos los ingredientes sobre tu mesa antes de encender el fuego.")
                }
                experticia.equals("Avanzado", ignoreCase = true) || experticia.equals("Intermedio", ignoreCase = true) -> {
                    consejosLocales.add("💡 Esta receta de '$titulo' es ideal para tu nivel $experticia. Puedes ajustar los tiempos de dorado y sazonado para resaltar sabores.")
                }
                else -> {
                    consejosLocales.add("💡 La receta '$titulo' es totalmente compatible con tu perfil nutricional y preferencias.")
                }
            }
        }

        return consejosLocales.take(3)
    }
}
