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
import com.focusapp.data.AuthRepository
import com.focusapp.data.repository.SettingsRepository
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
    val authRepository = remember { AuthRepository(context) }
    val sessionViewModel = remember { SessionViewModel(context) }
    val settingsViewModel = remember { SettingsViewModel(context) }
    
    var currentScreen by remember { mutableStateOf("home") }
    var isAuthenticated by remember { mutableStateOf(authRepository.isAuthenticated()) }
    
    if (!isAuthenticated) {
        // Show authentication screen
        val theme by settingsViewModel.theme.collectAsState()
        AuthenticationScreen(
            onSignIn = { email, password ->
                val success = authRepository.signIn(email, password)
                if (success) {
                    isAuthenticated = true
                }
                success
            },
            onSignUp = { email, password ->
                val success = authRepository.signUp(email, password)
                if (success) {
                    isAuthenticated = true
                }
                success
            },
            theme = theme
        )
    } else {
        // Show main app screens
        when (currentScreen) {
            "home" -> HomeScreen(
                sessionViewModel = sessionViewModel,
                settingsViewModel = settingsViewModel,
                onNavigateToSettings = { currentScreen = "settings" }
            )
            
            "settings" -> SettingsScreen(
                settingsViewModel = settingsViewModel,
                onBack = { currentScreen = "home" }
            )
        }
    }
}
