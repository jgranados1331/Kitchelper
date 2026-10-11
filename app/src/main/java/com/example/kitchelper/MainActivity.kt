package com.example.kitchelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kitchelper.ui.theme.auth.ForgotPasswordScreen
import com.example.kitchelper.ui.theme.auth.LoginScreen
import com.example.kitchelper.ui.theme.auth.RegisterScreen
import com.example.kitchelper.ui.theme.home.HomeScreen
import com.example.kitchelper.ui.theme.profile.ProfileScreen
import com.example.kitchelper.ui.theme.favorites.FavoritesScreen
import com.example.kitchelper.ui.theme.recipes.RecipesScreen
import com.example.kitchelper.ui.theme.recipes.RecipeDetailScreen
import com.example.kitchelper.ui.theme.recipes.CreateRecipeScreen
import com.example.kitchelper.ui.theme.navigation.Routes
import com.example.kitchelper.ui.theme.KitchelperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KitchelperTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        // ===== MÓDULO 1: AUTENTICACIÓN =====
        composable(Routes.LOGIN) {
            LoginScreen(navController = navController)
        }
        composable(Routes.REGISTER) {
            RegisterScreen(navController = navController)
        }
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(navController = navController)
        }

        // ===== MÓDULO 2: HOME =====
        composable(Routes.HOME) {
            HomeScreen(navController = navController)
        }

        // ===== MÓDULO 3: PERFIL =====
        composable(Routes.PROFILE) {
            ProfileScreen(navController = navController)
        }

        // ===== MÓDULO 4: RECETAS =====
        composable(Routes.RECIPES) {
            RecipesScreen(navController = navController)
        }
        composable(Routes.FAVORITOS) {
            FavoritesScreen(navController = navController)
        }
        composable(
            route = Routes.RECIPE_DETAIL,
            arguments = listOf(androidx.navigation.navArgument("recetaId") {})
        ) { backStackEntry ->
            val recetaId = backStackEntry.arguments?.getString("recetaId") ?: ""
            RecipeDetailScreen(recetaId = recetaId, navController = navController)
        }

        // ===== MÓDULO 5: PRÁCTICAS (Pendiente) =====
        composable(
            route = Routes.PRACTICE_DETAIL,
            arguments = listOf(androidx.navigation.navArgument("practicaId") {})
        ) { backStackEntry ->
            val practicaId = backStackEntry.arguments?.getString("practicaId") ?: ""
            PlaceholderScreen("Práctica: $practicaId")
        }

        // ===== MÓDULO 6: AR (Pendiente) =====
        composable(Routes.AR_GUIDE) {
            PlaceholderScreen("Guía AR - Próximamente")
        }
        composable(
            route = Routes.AR_RECIPE,
            arguments = listOf(androidx.navigation.navArgument("recetaId") {})
        ) { backStackEntry ->
            val recetaId = backStackEntry.arguments?.getString("recetaId") ?: ""
            PlaceholderScreen("AR Receta: $recetaId")
        }
        composable(
            route = Routes.AR_PRACTICE,
            arguments = listOf(androidx.navigation.navArgument("practicaId") {})
        ) { backStackEntry ->
            val practicaId = backStackEntry.arguments?.getString("practicaId") ?: ""
            PlaceholderScreen("AR Práctica: $practicaId")
        }

        // ===== MÓDULO 7: CREAR RECETA =====
        composable(Routes.CREATE_RECIPE) {
            CreateRecipeScreen(navController = navController)
        }
    }
}

/**
 * Pantalla temporal que se muestra mientras se construyen los módulos restantes.
 * Se eliminará cuando todas las pantallas estén implementadas.
 */
@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
}