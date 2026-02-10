package com.focusapp.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.focusapp.data.repository.SettingsRepository
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.focusapp.ui.screens.*
import com.focusapp.ui.theme.FocusAppTheme
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply saved language on activity creation
        applySavedLanguage()
        
        setContent {
            FocusAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FocusApp()
                }
            }
        }
    }
    
    private fun applySavedLanguage() {
        val settingsRepository = SettingsRepository(this)
        val languageCode = settingsRepository.getLanguage()
        updateLocale(languageCode)
    }
    
    private fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}

@Composable
fun FocusApp() {
    val context = LocalContext.current
    val sessionViewModel = remember { SessionViewModel(context) }
    val settingsViewModel = remember { SettingsViewModel(context) }
    
    val navController = rememberNavController()
    
    // Show main app screens directly (no authentication required)
    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
    ) {
        composable("home") {
            HomeScreen(
                sessionViewModel = sessionViewModel,
                settingsViewModel = settingsViewModel,
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
