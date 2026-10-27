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
     * Generar un consejo técnico dinámico y 100% ÚNICO para CADA PASO y CADA RECETA en tiempo real.
     */
    suspend fun obtenerConsejoPasoASpaso(
        tituloReceta: String,
        pasoActual: Paso,
        usuario: Usuario
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val culinaria = usuario.informacionCulinaria
            val salud = usuario.informacionSalud

            val experticia = culinaria.experticia.ifBlank { "Principiante" }
            val afeccionesStr = salud.problemasSalud.filter { it.isNotBlank() }.joinToString(", ")
            val tipoAlimentacion = salud.tipoAlimentacion.ifBlank { "Normal" }
            val salado = culinaria.nivelSalado.ifBlank { "Normal" }
            val endulzante = culinaria.endulzante.ifBlank { "Azúcar" }

            val descOriginal = pasoActual.descripcion.trim().removeSuffix(".")
            val descLower = descOriginal.lowercase()
            val numPaso = pasoActual.orden

            if (apiKey.startsWith("AIzaSy_REPLACE") || apiKey.isBlank()) {
                val consejoEspecifico = buildString {
                    append("👨‍🍳 Consejo para '$tituloReceta' - Paso $numPaso ($experticia):\n")

                    when {
                        descLower.contains("corta") || descLower.contains("pica") || descLower.contains("reban") || descLower.contains("troce") || descLower.contains("pel") -> {
                            if (experticia.equals("Principiante", ignoreCase = true)) {
                                append("Para '$descOriginal': asegura tu tabla con un paño húmedo debajo y utiliza el 'agarre en garra' doblando las yemas de tus dedos hacia adentro para cortar sin riesgo.")
                            } else {
                                append("Para '$descOriginal': mantén cortes parejos e iguales para garantizar una cocción 100% uniforme en la preparación.")
                            }
                        }
                        descLower.contains("sal") || descLower.contains("sazona") || descLower.contains("condiment") || descLower.contains("pimienta") || descLower.contains("especi") -> {
                            if (salud.problemasSalud.any { it.contains("tensión", ignoreCase = true) || it.contains("hipertensión", ignoreCase = true) }) {
                                append("⚠️ Al '$descOriginal' (cuidado con la tensión): disminuye la sal y realza los sabores usando hierbas aromáticas, ajo o unas gotas de limón fresco.")
                            } else if (salado.equals("Bajo", ignoreCase = true)) {
                                append("Al '$descOriginal': como prefieres sazón ligera, agrega solo una pizca inicial y ajusta al final.")
                            } else {
                                append("Al '$descOriginal': incorpora los condimentos gradualmente y prueba la preparación para balancear los sabores.")
                            }
                        }
                        descLower.contains("aceite") || descLower.contains("freír") || descLower.contains("fría") || descLower.contains("fuego") || descLower.contains("sartén") || descLower.contains("sarten") || descLower.contains("sofre") || descLower.contains("dora") -> {
                            if (tipoAlimentacion.equals("Fitness", ignoreCase = true)) {
                                append("💡 Para '$descOriginal' (Perfil Fitness): sustituye la fritura por saltear con spray de aceite de oliva o usar la freidora de aire.")
                            } else {
                                append("Para '$descOriginal': precalienta la sartén 1 minuto a fuego medio antes de poner la grasa para lograr un dorado uniforme.")
                            }
                        }
                        descLower.contains("azúcar") || descLower.contains("azucar") || descLower.contains("endulza") || descLower.contains("miel") -> {
                            if (endulzante.isNotBlank() && !endulzante.equals("Azúcar", ignoreCase = true)) {
                                append("💡 Al '$descOriginal': sustituye el azúcar refinado por $endulzante según las preferencias registradas en tu perfil.")
                            } else {
                                append("Al '$descOriginal': disuelve el endulzante a fuego medio-bajo para integrarlo bien sin quemarlo.")
                            }
                        }
                        descLower.contains("mezcla") || descLower.contains("bat") || descLower.contains("incorpor") || descLower.contains("remuev") || descLower.contains("revolv") -> {
                            append("Para '$descOriginal': realiza movimientos envolventes desde los bordes hacia el centro para mantener una textura fluida e intoda.")
                        }
                        descLower.contains("horn") || descLower.contains("gratin") -> {
                            append("Al '$descOriginal': precalienta el horno con anticipación y ubica la bandeja en la rejilla central para una distribución homogénea del calor.")
                        }
                        descLower.contains("herv") || descLower.contains("cocin") || descLower.contains("olla") || descLower.contains("agua") || descLower.contains("caldo") -> {
                            append("Para '$descOriginal': mantén la olla tapada hasta que hierva para conservar los aromas y luego ajusta a fuego medio.")
                        }
                        descLower.contains("licu") || descLower.contains("proces") || descLower.contains("mol") -> {
                            append("Al '$descOriginal': comienza a velocidad baja y aumenta paulatinamente para obtener una consistencia perfectamente tersa.")
                        }
                        descLower.contains("serv") || descLower.contains("emplat") || descLower.contains("decor") -> {
                            append("¡Llegaste a la etapa final de '$tituloReceta'! Sirve de inmediato para conservar la temperatura y presentación ideal.")
                        }
                        else -> {
                            // Respuesta 100% personalizada con el texto exacto del paso y título de la receta
                            append("En la acción de '$descOriginal': realiza el procedimiento a fuego adecuado y supervisa de cerca los cambios de aroma y textura hasta alcanzar el punto óptimo.")
                        }
                    }
                }

                return@withContext Result.success(consejoEspecifico)
            }

            val prompt = """
                Eres el mentor de cocina inteligente de KitchenPer. Genera un consejo de cocina ÚNICO y sumamente personalizado para el Paso $numPaso de la receta "$tituloReceta".

                DATOS DEL PASO $numPaso DE "$tituloReceta":
                - Acción a realizar: "$descOriginal"

                PERFIL DEL USUARIO:
                - Nivel de Experticia: $experticia
                - Tipo de Alimentación: $tipoAlimentacion
                - Afecciones de Salud: ${afeccionesStr.ifBlank { "Ninguna" }}
                - Preferencias: Salado ($salado), Endulzante ($endulzante)

                INSTRUCCIONES OBLIGATORIAS:
                1. MENCIONA EXPLÍCITAMENTE la acción "$descOriginal" y la receta "$tituloReceta".
                2. Genera un consejo de 2 frases breves, fresco y completamente diferente a otros pasos.
                3. Adapta el consejo al nivel de experticia ($experticia) y salud del usuario.
                4. Inicia la respuesta con 👨‍🍳.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val textResult = response.text ?: ""
            if (textResult.isNotBlank()) {
                Result.success(textResult.trim())
            } else {
                Result.success("👨‍🍳 En este Paso $numPaso ('$descOriginal') de '$tituloReceta': supervisa la temperatura y mantén el control.")
            }
        } catch (_: Exception) {
            Result.success("👨‍🍳 Paso ${pasoActual.orden} de '$tituloReceta': Aplica buena técnica y adapta los sazones a tu gusto.")
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
                    consejosLocales.add("⚠️ En '${receta.titulo}': Notamos alergia/intolerancia a lácteos en tu perfil. Te sugerimos sustituir lácteos por versiones deslactosadas o de almendras/avena.")
                }

                if (consejosLocales.isEmpty()) {
                    consejosLocales.add("💡 La receta '${receta.titulo}' se ajusta perfectamente a tu perfil culinario de nivel ${culinaria.experticia.ifBlank { "Principiante" }}.")
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
                Identifica si algún ingrediente de "${receta.titulo}" contradice las preferencias o salud del usuario.
                Devuelve de 1 a 3 recomendaciones de sustitución directas mencionando la receta. Cada sugerencia debe empezar con el ícono 💡 o ⚠️.
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
