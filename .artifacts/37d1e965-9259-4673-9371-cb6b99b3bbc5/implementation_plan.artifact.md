# Plan: Personalización Inteligente del Paso a Paso Guiado con IA (Experticia, Salud y Preferencias)

Este plan detalla el ajuste de los prompts y de la lógica de recomendación en el repositorio de IA (`GeminiAiRepository.kt`) y la pantalla de paso a paso (`RecipeStepViewerScreen.kt`), con el objetivo de adaptar de forma hiper-personalizada la guía interactiva para cada usuario según su clasificación completa en Firestore.

## User Review Required

> [!IMPORTANT]
> - **Inclusión Completa del Perfil del Usuario**: Los prompts de Gemini IA para cada paso de la receta analizarán en tiempo real:
>   1. **Experticia** (Principiante, Intermedio, Avanzado).
>   2. **Salud y Nutrición** (Alergias, afecciones como tensión alta/diabetes, tipo de alimentación como Fitness/Vegana).
>   3. **Preferencias Culinarias** (Nivel de salado, dulce, picante, condimentos y endulzante preferido).
> - **Motor de Respaldo Local (Offline / Fallback)**: Se mejorará la lógica local para que, incluso sin conexión a la API de Gemini, genere consejos técnicos personalizados según las alergias, afecciones y nivel de experticia del usuario.

## Proposed Changes

### Repositorio de IA (`data/repository/`)

#### [MODIFY] [GeminiAiRepository.kt](file:///C:/Users/johan/OneDrive/Documents/universidad/Kitchelper/Kitchelper/app/src/main/java/com/example/kitchelper/data/repository/GeminiAiRepository.kt)
- Reestructurar la función `obtenerConsejoPasoASpaso(tituloReceta, pasoActual, usuario)` para integrar:
  - Prompt enriquecido con datos completos de salud (`alergias`, `problemasSalud`, `tipoAlimentacion`), experticia (`experticia`) y preferencias de sabor (`nivelSalado`, `nivelDulce`, `endulzante`, `nivelCondimento`).
  - Lógica de fallback inteligente enriquecida con reglas personalizadas por afección de salud (ej. reducción de sodio para hipertensión, técnica con cuchillo/temperatura para principiantes, sustitución para intolerancias).

---

### Visualizador de Paso a Paso (`ui/theme/recipes/`)

#### [MODIFY] [RecipeStepViewerScreen.kt](file:///C:/Users/johan/OneDrive/Documents/universidad/Kitchelper/Kitchelper/app/src/main/java/com/example/kitchelper/ui/theme/recipes/RecipeStepViewerScreen.kt)
- Ajustar la sección de recomendaciones de la IA para presentar:
  - **👨‍🍳 Consejo del Chef Personalizado** (adaptado al nivel y acción del paso).
  - **⚠️ Alerta de Salud / Ajuste Culinario** (si el paso involucra sal, frituras, azúcar o alérgenos declarados en el perfil del usuario).

## Verification Plan

### Automated Tests
- Compilación del proyecto (`gradle_build`) para verificar cero errores sintácticos o de tipos.

### Manual Verification
- Probar el visualizador de paso a paso con diferentes usuarios (ej. un usuario principiante con hipertensión vs un usuario avanzado fitness) y comprobar cómo se personaliza el consejo y la adaptación del paso en tiempo real.
