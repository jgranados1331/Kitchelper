# Plan: Implementación de la Sección de Recetas (`RecipesScreen`) y Guía de Subida de Imágenes a Firebase Storage

Este plan detalla la creación de la pantalla y el ViewModel para la sección de Recetas (`Routes.RECIPES`), conectándola con `RecipeRepository` y Firestore, así como una guía completa sobre cómo subir imágenes a **Firebase Storage** y almacenarlas en **Firestore**.

## User Review Required

> [!IMPORTANT]
> - **Pantalla de Recetas**: Se implementará `RecipesScreen.kt`, `RecipesViewModel.kt` y `RecipeDetailScreen.kt` para reemplazar los placeholders actuales en `MainActivity.kt`.
> - **Guía de Almacenamiento de Imágenes**: Se proveerá una recomendación arquitectónica y de código utilizando **Firebase Storage** junto con **Cloud Firestore**.

## Proposed Changes

### Módulo de Recetas (`ui/theme/recipes/` o similar)

#### [NEW] [RecipesViewModel.kt](file:///C:/Users/johan/OneDrive/Documents/universidad/Kitchelper/Kitchelper/app/src/main/java/com/example/kitchelper/ui/theme/recipes/RecipesViewModel.kt)
- ViewModel para gestionar la lista de recetas, búsqueda, filtrado por categoría y dificultad desde `RecipeRepository`.

#### [NEW] [RecipesScreen.kt](file:///C:/Users/johan/OneDrive/Documents/universidad/Kitchelper/Kitchelper/app/src/main/java/com/example/kitchelper/ui/theme/recipes/RecipesScreen.kt)
- Pantalla visual con barra de búsqueda, filtros de categoría y tarjetas de recetas con Coil (`AsyncImage`).

#### [NEW] [RecipeDetailScreen.kt](file:///C:/Users/johan/OneDrive/Documents/universidad/Kitchelper/Kitchelper/app/src/main/java/com/example/kitchelper/ui/theme/recipes/RecipeDetailScreen.kt)
- Pantalla de detalle para visualizar ingredientes, pasos y tiempo de la receta.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/johan/OneDrive/Documents/universidad/Kitchelper/Kitchelper/app/src/main/java/com/example/kitchelper/MainActivity.kt)
- Reemplazar los placeholders de `Routes.RECIPES` y `Routes.RECIPE_DETAIL` por las pantallas reales.

---

### Guía de Subida de Imágenes a Firebase Storage (Artifact / Explicación)
- Explicación detallada del flujo: Compresión local $\rightarrow$ Firebase Storage (`storage.reference`) $\rightarrow$ Obtención de `downloadUrl` $\rightarrow$ Guardado en Firestore.

## Verification Plan

### Automated Tests
- Compilación del proyecto (`gradle_build`) para verificar cero errores de compilación.

### Manual Verification
- Navegar a la sección de Recetas desde la barra inferior de la Home y verificar que carguen desde Firestore.
