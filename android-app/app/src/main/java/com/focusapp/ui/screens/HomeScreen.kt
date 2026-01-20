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
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val sessionState by sessionViewModel.sessionState.collectAsState()
    val currentSession by sessionViewModel.currentSession.collectAsState()
    val weeklyStats by sessionViewModel.weeklyStats.collectAsState()
    
    LaunchedEffect(Unit) {
        sessionViewModel.loadWeeklyStats()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E2E))
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
                    Row {
                        IconButton(onClick = onNavigateToSettings) {
                            Text("⚙️", style = MaterialTheme.typography.headlineSmall)
                        }
                        IconButton(onClick = onLogout) {
                            Text("🚪", style = MaterialTheme.typography.headlineSmall)
                        }
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
                            Text(
                                text = "Started at: ${currentSession?.startTime?.take(16)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
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
                                Button(
                                    onClick = { sessionViewModel.endSession() },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = sessionState !is SessionState.Loading,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Stop Session")
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
