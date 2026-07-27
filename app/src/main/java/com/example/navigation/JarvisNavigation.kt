package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MainAssistantScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.SettingsScreen

object JarvisRoutes {
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val MEMORY = "memory"
    const val HISTORY = "history"
}

@Composable
fun JarvisNavigation(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = JarvisRoutes.MAIN,
        modifier = modifier
    ) {
        composable(JarvisRoutes.MAIN) {
            MainAssistantScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate(JarvisRoutes.SETTINGS) },
                onNavigateToMemory = { navController.navigate(JarvisRoutes.MEMORY) },
                onNavigateToHistory = { navController.navigate(JarvisRoutes.HISTORY) }
            )
        }

        composable(JarvisRoutes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(JarvisRoutes.MEMORY) {
            MemoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(JarvisRoutes.HISTORY) {
            HistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
