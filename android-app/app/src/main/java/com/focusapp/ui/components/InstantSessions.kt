package com.focusapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusapp.ui.theme.GlassSurface
import kotlinx.coroutines.delay

@Composable
fun InstantSessions(
    onStart: (minutes: Int) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMinutes by remember { mutableStateOf<Int?>(null) }
    var isRunning by remember { mutableStateOf(false) }
    var totalSeconds by remember { mutableStateOf(0) }
    var elapsedSeconds by remember { mutableStateOf(0) }

    // Timer logic
    LaunchedEffect(isRunning, totalSeconds) {
        if (isRunning && totalSeconds > 0) {
            while (elapsedSeconds < totalSeconds) {
                delay(1000L)
                elapsedSeconds++
            }
            // Timer completed
            isRunning = false
            onComplete()
            // Reset state
            selectedMinutes = null
            elapsedSeconds = 0
            totalSeconds = 0
        }
    }

    val rawProgress = if (totalSeconds > 0) {
        elapsedSeconds.toFloat() / totalSeconds.toFloat()
    } else 0f
    
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        label = "progress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurface)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Instant Sessions",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            // Circular progress timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp)
            ) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    val trackColor = Color(0xFFDCD8F4).copy(alpha = 0.4f)
                    val progressColor = Color(0xFF6C4BB1)
                    
                    // Draw track
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                    
                    // Draw progress
                    if (animatedProgress > 0f) {
                        drawArc(
                            color = progressColor,
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
                
                // Center text
                Text(
                    text = if (isRunning) {
                        val remaining = totalSeconds - elapsedSeconds
                        val mins = remaining / 60
                        val secs = remaining % 60
                        String.format("%02d:%02d", mins, secs)
                    } else if (selectedMinutes != null) {
                        "${selectedMinutes}m"
                    } else {
                        "—"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            // Duration buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5, 15, 30, 45, 60).forEach { minutes ->
                    Button(
                        onClick = {
                            if (!isRunning) {
                                selectedMinutes = minutes
                                totalSeconds = minutes * 60
                                elapsedSeconds = 0
                                isRunning = true
                                onStart(minutes)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isRunning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMinutes == minutes && !isRunning) {
                                Color(0xFF6C4BB1)
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    ) {
                        Text(
                            text = "$minutes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Stop button (only shown when running)
            if (isRunning) {
                OutlinedButton(
                    onClick = {
                        isRunning = false
                        selectedMinutes = null
                        elapsedSeconds = 0
                        totalSeconds = 0
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Stop", color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E2E)
@Composable
fun InstantSessionsIdlePreview() {
    InstantSessions(
        onStart = {},
        onComplete = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E2E)
@Composable
fun InstantSessionsRunningPreview() {
    var isRunning by remember { mutableStateOf(true) }
    var totalSeconds by remember { mutableStateOf(15 * 60) }
    var elapsedSeconds by remember { mutableStateOf(5 * 60) }

    LaunchedEffect(Unit) {
        while (isRunning && elapsedSeconds < totalSeconds) {
            delay(1000L)
            elapsedSeconds++
        }
        if (elapsedSeconds >= totalSeconds) {
            isRunning = false
        }
    }

    val rawProgress = elapsedSeconds.toFloat() / totalSeconds.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        label = "progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurface)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Instant Sessions",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp)
            ) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    val trackColor = Color(0xFFDCD8F4).copy(alpha = 0.4f)
                    val progressColor = Color(0xFF6C4BB1)
                    
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                    
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                
                Text(
                    text = run {
                        val remaining = totalSeconds - elapsedSeconds
                        val mins = remaining / 60
                        val secs = remaining % 60
                        String.format("%02d:%02d", mins, secs)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5, 15, 30, 45, 60).forEach { minutes ->
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (minutes == 15) {
                                Color(0xFF6C4BB1)
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    ) {
                        Text(
                            text = "$minutes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Stop", color = Color.White)
            }
        }
    }
}
