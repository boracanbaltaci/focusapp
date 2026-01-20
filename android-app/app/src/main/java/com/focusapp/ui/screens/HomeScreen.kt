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
import com.focusapp.ui.theme.GlassSurface

@Composable
fun HomeScreen(
    sessionViewModel: SessionViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToSettings: () -> Unit
) {
    val sessionState by sessionViewModel.sessionState.collectAsState()
    val currentSession by sessionViewModel.currentSession.collectAsState()
    val weeklyStats by sessionViewModel.weeklyStats.collectAsState()
    val elapsedTime by sessionViewModel.elapsedTime.collectAsState()
    val isOnBreak by sessionViewModel.isOnBreak.collectAsState()
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
                // Session control card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (currentSession != null) "Session Active" else "Ready to Focus",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                        
                        if (currentSession != null) {
                            // Display timer
                            val hours = elapsedTime / 3600
                            val minutes = (elapsedTime % 3600) / 60
                            val seconds = elapsedTime % 60
                            Text(
                                text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                                style = MaterialTheme.typography.displayLarge,
                                color = if (isOnBreak) Color.Yellow else Color.White
                            )
                            
                            if (isOnBreak) {
                                Text(
                                    text = "On Break",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Yellow
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (currentSession == null) {
                                Button(
                                    onClick = { sessionViewModel.startSession(false) },
                                    modifier = Modifier.weight(1f),
                                    enabled = sessionState !is SessionState.Loading
                                ) {
                                    Text("Start Work")
                                }
                                
                                OutlinedButton(
                                    onClick = { sessionViewModel.startSession(true) },
                                    modifier = Modifier.weight(1f),
                                    enabled = sessionState !is SessionState.Loading
                                ) {
                                    Text("Start Break")
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { sessionViewModel.endSession() },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = sessionState !is SessionState.Loading,
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("End Session")
                                    }
                                    
                                    OutlinedButton(
                                        onClick = { sessionViewModel.toggleBreak() },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = sessionState !is SessionState.Loading
                                    ) {
                                        Text(if (isOnBreak) "Continue" else "Give a Break")
                                    }
                                }
                            }
                        }
                        
                        if (sessionState is SessionState.Error) {
                            Text(
                                text = (sessionState as SessionState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
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
