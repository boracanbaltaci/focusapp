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
    val authViewModel = remember { AuthViewModel(context) }
    val sessionViewModel = remember { SessionViewModel() }
    
    var currentScreen by remember { mutableStateOf("login") }
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    
    LaunchedEffect(isLoggedIn) {
        currentScreen = if (isLoggedIn) "home" else "login"
    }
    
    when (currentScreen) {
        "login" -> LoginScreen(
            authViewModel = authViewModel,
            onLoginSuccess = { currentScreen = "home" }
        )
        
        "home" -> HomeScreen(
            sessionViewModel = sessionViewModel,
            onNavigateToSettings = { currentScreen = "settings" },
            onLogout = { 
                authViewModel.logout()
                currentScreen = "login"
            }
        )
        
        "settings" -> SettingsScreen(
            onBack = { currentScreen = "home" }
        )
    }
}
