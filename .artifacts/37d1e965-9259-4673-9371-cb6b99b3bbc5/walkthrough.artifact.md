# Resumen de Cambios - Recálculo Dinámico e Individual por Paso de Receta

Se han solucionado los motivos que provocaban respuestas repetitivas o estáticas al avanzar o retroceder entre pasos:

## Cambios Realizados

### 1. Inclusión Dinámica del Texto Exacto del Paso (`GeminiAiRepository.kt`)
- Anteriormente, las descripciones que no contenían palabras clave específicas (como cortar o freír) caían en una cláusula de reserva estática idéntica para todos los pasos.
- Ahora, el generador de consejos analiza la acción exacta de cada paso (`pasoActual.descripcion`) e integra **el texto del paso y el título de la receta** directamente en la respuesta (ej. `"Para 'Mezclar la salsa en la sartén': realiza el procedimiento..."`).
- Se ampliaron las categorías de palabras clave (*mezclar*, *hornear*, *hervir*, *licuar*, *servir*, *sazonar*, *aceite*, *azúcar*, etc.) para dar un consejo técnico 100% único por paso.

### 2. Feedback Visual de Recálculo en Tiempo Real (`RecipeStepViewerScreen.kt`)
- Al presionar las flechas para avanzar o retroceder de paso, la pantalla inmediatamente:
  1. Limpia la sugerencia anterior (`consejoPasoActualIA = null`).
  2. Muestra un indicador de carga (`CircularProgressIndicator`) con el mensaje *"Generando consejo único para el Paso X de 'Título de Receta'..."*.
  3. Ejecuta de nuevo la recomendación de la IA para ese paso específico.

## Resultados de Verificación
- **Compilación**: Ejecución exitosa de `app:assembleDebug` con **Build finished successfully**.
