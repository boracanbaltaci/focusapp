package com.focusapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.focusapp.ui.screens.*
import com.focusapp.ui.theme.FocusAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}

@Composable
fun FocusApp() {
    val context = LocalContext.current
    val sessionViewModel = remember { SessionViewModel(context) }
    val settingsViewModel = remember { SettingsViewModel(context) }
    
    var currentScreen by remember { mutableStateOf("home") }
    
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
