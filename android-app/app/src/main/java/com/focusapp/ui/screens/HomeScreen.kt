package com.focusapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.focusapp.ui.components.StatsChart
import com.focusapp.ui.components.TimerDisplay
import com.focusapp.ui.theme.GlassSurface

@Composable
fun HomeScreen(
    sessionViewModel: SessionViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToSettings: () -> Unit
) {
    val weeklyStats by sessionViewModel.weeklyStats.collectAsState()
    val background by settingsViewModel.background.collectAsState()
    
    val backgroundColor = when (background) {
        "gradient" -> Color(0xFF2E1E3E)
        else -> Color(0xFF1E1E2E)
    }
    
    LaunchedEffect(Unit) {
        sessionViewModel.loadWeeklyStats()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Focus Timer",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    IconButton(onClick = onNavigateToSettings) {
                        Text("⚙️", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
            

            item {
                // Timer Display
                TimerDisplay()
            }
            
            item {
                // Weekly stats card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Weekly Progress",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (weeklyStats.isNotEmpty()) {
                            StatsChart(stats = weeklyStats)
                        } else {
                            Text(
                                text = "No data yet. Start tracking!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}
