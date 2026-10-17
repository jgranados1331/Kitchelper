package com.example.kitchelper.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.example.kitchelper.data.model.Paso
import com.example.kitchelper.data.model.Receta
import com.example.kitchelper.data.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiAiRepository {

    // Clave API de Gemini 1.5 Flash (Google AI Studio)
    private var apiKey = "AIzaSy_REPLACE_WITH_YOUR_GEMINI_KEY"

    private var generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    fun setApiKey(newKey: String) {
        if (newKey.isNotBlank()) {
            apiKey = newKey
            generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )
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
        try {
            if (apiKey.startsWith("AIzaSy_REPLACE") || apiKey.isBlank()) {
                val ingLower = nombreIngrediente.lowercase()
                val (precioStr, tiendaStr, tipStr) = when {
                    ingLower.contains("carne") || ingLower.contains("res") || ingLower.contains("pollo") || ingLower.contains("cerdo") ->
                        Triple("${'$'}12.000 - ${'$'}22.000 COP la libra", "Famas locales, Éxito, Carulla, Olímpica o D1", "Elige cortes de color rojo brillante o rosado fresco con marmoleado uniforme.")
                    ingLower.contains("leche") || ingLower.contains("queso") || ingLower.contains("crema") || ingLower.contains("mantequilla") ->
                        Triple("${'$'}3.500 - ${'$'}8.500 COP la unidad", "Tiendas D1, Ara, Carulla, Éxito o supermercados de barrio", "Verifica la fecha de vencimiento y mantenlo refrigerado entre 2°C y 4°C.")
                    ingLower.contains("tomate") || ingLower.contains("cebolla") || ingLower.contains("aguacate") || ingLower.contains("papa") || ingLower.contains("zanahoria") ->
                        Triple("${'$'}2.000 - ${'$'}5.500 COP la libra", "Plazas de mercado (Paloquemao / La Mayorista), fruterías locales o Éxito/Carulla", "Busca ejemplares de consistencia firme, piel limpia y sin magulladuras.")
                    ingLower.contains("harina") || ingLower.contains("azúcar") || ingLower.contains("azucar") || ingLower.contains("arroz") || ingLower.contains("aceite") ->
                        Triple("${'$'}3.200 - ${'$'}7.000 COP la presentación", "Tiendas Ara, D1, Éxito, Olímpica y tiendas de víveres", "Consérvalo en un recipiente hermético en un lugar fresco y seco alejado de la humedad.")
                    else ->
                        Triple("${'$'}3.000 - ${'$'}8.000 COP la porción", "Supermercados como Éxito, Carulla, D1, Ara y plazas de mercado", "Verifica su textura, aroma característico y empaque sellado.")
                }

                return@withContext Result.success(
                    "🛒 Dónde comprar $nombreIngrediente en $ubicacionUsuario:\n$tiendaStr.\n\n" +
                    "💰 Precio estimado:\n$precioStr.\n\n" +
                    "💡 Consejo útil:\n$tipStr"
                )
            }

            val prompt = """
                Eres el asistente de compras de la app KitchenPer. Un usuario en "$ubicacionUsuario" necesita comprar el ingrediente "$nombreIngrediente" para su receta.
                
                Responde en máximo 3 frases breves con datos específicos de Colombia con este formato exacto:
                🛒 Dónde comprar: (menciona cadenas exactas como Éxito, Carulla, D1, Ara, Olímpica o plazas de mercado)
                💰 Precio estimado: (menciona un rango estimado en pesos colombianos COP $, ejemplo ${'$'}3.000 - ${'$'}6.000 COP)
                💡 Consejo útil: (un consejo único de frescura o selección para $nombreIngrediente)
                
                Sé muy directo, conciso y amigable.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val textResult = response.text ?: ""
            if (textResult.isNotBlank()) {
                Result.success(textResult.trim())
            } else {
                Result.success("🛒 Dónde comprar $nombreIngrediente: Supermercados locales y plazas de mercado en Colombia.\n💰 Precio estimado: Rango promedio comercial.\n💡 Consejo útil: Selecciona productos de aroma y textura fresca.")
            }
        } catch (_: Exception) {
            Result.success(
                "🛒 Dónde comprar $nombreIngrediente: Cadenas locales como D1, Éxito, Carulla, Ara y plazas de mercado en Colombia.\n💰 Precio estimado: Rango promedio en pesos colombianos COP $.\n💡 Consejo útil: Conserva el ingrediente en un lugar fresco y seco."
            )
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
        try {
            val nivel = usuario.informacionCulinaria.experticia.ifBlank { "Principiante" }
            val descLower = pasoActual.descripcion.lowercase()

            if (apiKey.startsWith("AIzaSy_REPLACE") || apiKey.isBlank()) {
                val consejoEspecifico = when {
                    descLower.contains("corta") || descLower.contains("pica") || descLower.contains("reban") || descLower.contains("troce") ->
                        "👨‍🍳 Consejo para el Paso ${pasoActual.orden}: Coloca un paño húmedo bajo tu tabla de picar para fijarla. Usa el 'agarre en garra' doblando las yemas de tus dedos hacia adentro para cortar con total seguridad."
                    descLower.contains("fuego") || descLower.contains("calient") || descLower.contains("sofre") || descLower.contains("saltea") || descLower.contains("sartén") || descLower.contains("sarten") ->
                        "👨‍🍳 Consejo para el Paso ${pasoActual.orden}: Precalienta el sartén 1 minuto antes de agregar grasa. Si el aceite forma ondas brillantes en la superficie, está listo para recibir los alimentos."
                    descLower.contains("herv") || descLower.contains("cocin") || descLower.contains("agua") || descLower.contains("olla") ->
                        "👨‍🍳 Consejo para el Paso ${pasoActual.orden}: Mantén la olla tapada hasta alcanzar el punto de ebullición para ahorrar tiempo y energía, luego ajusta a fuego medio para una cocción pareja."
                    descLower.contains("mezcla") || descLower.contains("bat") || descLower.contains("incorpor") || descLower.contains("remuev") ->
                        "👨‍🍳 Consejo para el Paso ${pasoActual.orden}: Mezcla con movimientos envolventes desde los bordes hacia el centro del recipiente para mantener la homogeneidad y textura deseada."
                    descLower.contains("horno") || descLower.contains("horne") ->
                        "👨‍🍳 Consejo para el Paso ${pasoActual.orden}: Asegúrate de precalentar el horno al menos 10 minutos antes. Coloca el molde en la rejilla central para una distribución uniforme del calor."
                    else ->
                        "👨‍🍳 Consejo para el Paso ${pasoActual.orden}: Presta atención a los cambios de color y textura de los ingredientes. Mantén tu área de trabajo ordenada. ¡Vas excelente!"
                }

                return@withContext Result.success(consejoEspecifico)
            }

            val prompt = """
                Eres el mentor de cocina de KitchenPer. El usuario de nivel "$nivel" está ejecutando el Paso ${pasoActual.orden} de la receta "$tituloReceta".
                
                DESCRIPCIÓN ESPECÍFICA DEL PASO ${pasoActual.orden}:
                "${pasoActual.descripcion}"
                
                Instrucciones: Genera 1 o 2 frases breves, muy amables y motivadoras con un consejo técnico clave (ejemplo: seguridad en corte, dorados, aromas, textura o punto de cocción) enfocado ÚNICAMENTE en las acciones de esta descripción.
                Inicia la respuesta con 👨‍🍳.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val textResult = response.text ?: ""
            if (textResult.isNotBlank()) {
                Result.success(textResult.trim())
            } else {
                Result.success("👨‍🍳 ¡Excelente trabajo! Mantén el control de la preparación y disfruta el proceso.")
            }
        } catch (_: Exception) {
            Result.success("👨‍🍳 Consejo de cocina para el Paso ${pasoActual.orden}: Mantén un fuego bien controlado y ten todo a la mano. ¡Vas muy bien!")
        }
    }

    /**
     * Generar adaptaciones y consejos de sustitución de ingredientes personalizados para la receta completa.
     */
    suspend fun generarSugerenciasPersonalizadasReceta(
        usuario: Usuario,
        receta: Receta
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val culinaria = usuario.informacionCulinaria
            val salud = usuario.informacionSalud

            val alergiasStr = salud.alergias.filter { it.isNotBlank() }.joinToString(", ")
            val restriccionesStr = salud.restricciones.filter { it.isNotBlank() }.joinToString(", ")
            val problemasStr = salud.problemasSalud.filter { it.isNotBlank() }.joinToString(", ")
            val ingredientesStr = receta.ingredientes.joinToString(", ") { "${it.nombre} (${it.cantidad})" }

            if (apiKey.startsWith("AIzaSy_REPLACE") || apiKey.isBlank()) {
                val consejosLocales = mutableListOf<String>()

                if (culinaria.endulzante.isNotBlank() && !culinaria.endulzante.equals("Azúcar", ignoreCase = true) &&
                    receta.ingredientes.any { it.nombre.contains("azúcar", ignoreCase = true) || it.nombre.contains("azucar", ignoreCase = true) }) {
                    consejosLocales.add("💡 Tu perfil indica preferencia por ${culinaria.endulzante}. Puedes sustituir el azúcar de esta receta por ${culinaria.endulzante} al gusto.")
                }

                if (salud.tipoAlimentacion.equals("Fitness", ignoreCase = true)) {
                    consejosLocales.add("💡 Para mantener tu perfil Fitness, te recomendamos sustituir aceites tradicionales por freidora de aire o aceite de oliva.")
                }

                if (salud.alergias.any { it.contains("lactosa", ignoreCase = true) || it.contains("lácteos", ignoreCase = true) } &&
                    receta.ingredientes.any { it.nombre.contains("leche", ignoreCase = true) || it.nombre.contains("queso", ignoreCase = true) || it.nombre.contains("crema", ignoreCase = true) }) {
                    consejosLocales.add("⚠️ Notamos intolerancia a lácteos en tu perfil. Te sugerimos sustituir ingredientes lácteos por leche de almendras, avena o deslactosada.")
                }

                if (consejosLocales.isEmpty()) {
                    consejosLocales.add("💡 La receta se ajusta muy bien a tu perfil culinario de nivel ${culinaria.experticia.ifBlank { "Principiante" }}.")
                }

                return@withContext Result.success(consejosLocales)
            }

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
                Identifica si algún ingrediente de la receta contradice las preferencias, alergias o estilo de alimentación del usuario (ejemplo: si prefiere Stevia y la receta usa Azúcar, o si padece de alergia a la lactosa/gluten y la receta usa leche o harina).
                Devuelve de 1 a 3 recomendaciones de sustitución prácticas, amigables y directas. Cada sugerencia debe empezar con el ícono 💡 o ⚠️.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val textResult = response.text ?: ""

            if (textResult.isNotBlank()) {
                val lineas = textResult.lines()
                    .map { it.trim() }
                    .filter { it.isNotBlank() && (it.startsWith("💡") || it.startsWith("⚠️") || it.startsWith("-") || it.startsWith("*")) }
                    .map { it.removePrefix("- ").removePrefix("* ") }

                if (lineas.isNotEmpty()) {
                    Result.success(lineas)
                } else {
                    Result.success(listOf("💡 Esta receta es adecuada para tu perfil culinario."))
                }
            } else {
                Result.success(listOf("💡 La receta encaja muy bien con tus hábitos registrados."))
            }
        } catch (_: Exception) {
            Result.success(listOf("💡 La receta se adapta a tu perfil registrado en KitchenPer."))
        }
    }
}
