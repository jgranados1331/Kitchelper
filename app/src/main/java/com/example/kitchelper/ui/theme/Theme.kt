package com.example.kitchelper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = VerdeKitchenDark,
    onPrimary = OnVerdeKitchenDark,
    primaryContainer = VerdeKitchenContainerDark,
    onPrimaryContainer = OnVerdeKitchenContainerDark,
    secondary = NaranjaAccentDark,
    onSecondary = OnNaranjaAccentDark,
    secondaryContainer = NaranjaContainerDark,
    onSecondaryContainer = OnNaranjaContainerDark,
    background = CremaBackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = VerdeKitchenLight,
    onPrimary = OnVerdeKitchenLight,
    primaryContainer = VerdeKitchenContainerLight,
    onPrimaryContainer = OnVerdeKitchenContainerLight,
    secondary = NaranjaAccentLight,
    onSecondary = OnNaranjaAccentLight,
    secondaryContainer = NaranjaContainerLight,
    onSecondaryContainer = OnNaranjaContainerLight,
    background = CremaBackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight
)

@Composable
fun KitchelperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
