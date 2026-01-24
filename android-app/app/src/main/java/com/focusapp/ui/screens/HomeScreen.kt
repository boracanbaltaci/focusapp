package com.focusapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.focusapp.ui.components.TimerDisplay

@Composable
fun HomeScreen(
    sessionViewModel: SessionViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToSettings: () -> Unit
) {
    val background by settingsViewModel.background.collectAsState()
    
    val backgroundColor = when (background) {
        "gradient" -> Color(0xFF2E1E3E)
        else -> Color(0xFF1E1E2E)
    }
    
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        if (isLandscape) {
            // Landscape layout: Settings button and timer side by side
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settings button on the left in landscape
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.align(Alignment.Top)
                ) {
                    Text("⚙️", style = MaterialTheme.typography.headlineSmall)
                }
                
                // Timer in the center
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    TimerDisplay()
                }
                
                // Spacer for balance
                Spacer(modifier = Modifier.width(48.dp))
            }
        } else {
            // Portrait layout: Original vertical layout
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Settings button at top right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateToSettings) {
                            Text("⚙️", style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
                

                item {
                    // Timer Display
                    TimerDisplay()
                }
            }
        }
    }
}
