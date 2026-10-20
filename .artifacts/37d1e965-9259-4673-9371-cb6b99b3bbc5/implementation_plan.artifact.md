# Plan: Mejora del Reconocimiento de Ingredientes ARCore y Diálogo de Selección (Receta Existente vs Receta Improvisada con IA)

Este plan detalla las mejoras al escáner ARCore / IA:
1. **Mejorar el Reconocimiento**: Ampliar el catálogo de tokens en `IngredientAnalyzer.kt` y ajustar el umbral de confianza a `0.45f` para una detección más fluida y precisa.
2. **Flujo Post-Escaneo (Diálogo de Decisión)**: Al presionar el botón "Usar X ingredientes en la receta" en `ArIngredientScannerScreen.kt`, mostrar un diálogo con dos opciones:
   - **Buscar receta existente**: Buscar en el repositorio de recetas si alguna contiene los ingredientes escaneados.
   - **Receta improvisada con IA**: Generar una receta improvisada basada en los ingredientes escaneados (prellenando el flujo de creación de receta con título, ingredientes y pasos sugeridos por IA).

## User Review Required

> [!IMPORTANT]
> - **Diálogo Interactivo Post-Escaneo**: Se añadirá un diálogo al confirmar los ingredientes escaneados para que el usuario elija entre buscar una receta existente o crear una receta improvisada con IA.

## Proposed Changes

### Escáner AR / IA (`ar/`)

#### [MODIFY] [IngredientAnalyzer.kt](file:///C:/Users/johan/OneDrive/Documents/universidad/Kitchelper/Kitchelper/app/src/main/java/com/example/kitchelper/ar/IngredientAnalyzer.kt)
- Reducir el umbral de confianza por defecto a `0.45f` para mayor sensibilidad de reconocimiento.
- Ampliar la whitelist de `INGREDIENT_TOKENS` y `SPANISH_NAMES` con más variedades culinarias.

#### [MODIFY] [ArIngredientScannerScreen.kt](file:///C:/Users/johan/OneDrive/Documents/universidad/Kitchelper/Kitchelper/app/src/main/java/com/example/kitchelper/ar/ArIngredientScannerScreen.kt)
- Añadir un estado de diálogo al presionar el botón de confirmación de ingredientes.
- Opción 1: Buscar receta existente (filtrando por ingredientes).
- Opción 2: Generar receta improvisada (creando un borrador en `CreateRecipeScreen` con los ingredientes escaneados y título/pasos generados).

---

## Verification Plan

### Automated Tests
- Compilación del proyecto (`gradle_build`) para verificar cero errores de compilación.

### Manual Verification
- Probar el escaneo AR en la app y verificar que al confirmar aparezca el diálogo para elegir entre receta existente o receta improvisada con IA.
