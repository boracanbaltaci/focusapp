package com.focusapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusapp.ui.theme.GlassSurface
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun InstantSessions(
    onStart: (minutes: Int) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMinutes by remember { mutableStateOf(15) }
    var isRunning by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var totalSeconds by remember { mutableStateOf(0) }
    var elapsedSeconds by remember { mutableStateOf(0) }

    // Timer logic
    LaunchedEffect(isRunning, isPaused, totalSeconds) {
        if (isRunning && !isPaused && totalSeconds > 0) {
            while (elapsedSeconds < totalSeconds && isRunning && !isPaused) {
                delay(1000L)
                elapsedSeconds++
            }
            // Timer completed
            if (elapsedSeconds >= totalSeconds) {
                isRunning = false
                isPaused = false
                onStop()
                // Reset state
                elapsedSeconds = 0
                totalSeconds = 0
            }
        }
    }

    val progress = if (totalSeconds > 0) {
        elapsedSeconds.toFloat() / totalSeconds.toFloat()
    } else 0f
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
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

            if (!isRunning) {
                // Duration selector
                Text(
                    text = "Select Duration",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                DurationSelector(
                    selectedMinutes = selectedMinutes,
                    onSelectMinutes = { selectedMinutes = it }
                )

                // Start button
                Button(
                    onClick = {
                        totalSeconds = selectedMinutes * 60
                        elapsedSeconds = 0
                        isRunning = true
                        isPaused = false
                        onStart(selectedMinutes)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start")
                }
            } else {
                // Rounded-square timer display with animated ticks
                RoundedSquareTimer(
                    remainingSeconds = totalSeconds - elapsedSeconds,
                    progress = animatedProgress,
                    isRunning = !isPaused
                )

                // Pause/Resume and Stop buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (isPaused) {
                                isPaused = false
                                onResume()
                            } else {
                                isPaused = true
                                onPause()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isPaused) "Resume" else "Pause")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            isRunning = false
                            isPaused = false
                            elapsedSeconds = 0
                            totalSeconds = 0
                            onStop()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Stop", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun DurationSelector(
    selectedMinutes: Int,
    onSelectMinutes: (Int) -> Unit
) {
    val durations = listOf(5, 10, 15, 30, 45, 60)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        durations.forEach { minutes ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(
                        color = if (selectedMinutes == minutes) {
                            Color(0xFF6C4BB1)
                        } else {
                            Color.White.copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectMinutes(minutes) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$minutes",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = if (selectedMinutes == minutes) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun RoundedSquareTimer(
    remainingSeconds: Int,
    progress: Float,
    isRunning: Boolean
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)
    
    // Infinite transition for tick animation
    val infiniteTransition = rememberInfiniteTransition(label = "tickAnimation")
    val oscillation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "oscillation"
    )
    
    Box(
        modifier = Modifier.size(96.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val cornerRadius = 18.dp.toPx()
            val tickCount = 32
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radiusOuter = canvasSize / 2 - 8.dp.toPx()
            val radiusInner = radiusOuter - 8.dp.toPx()
            
            // Draw rounded square background
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(8.dp.toPx(), 8.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(
                    canvasSize - 16.dp.toPx(),
                    canvasSize - 16.dp.toPx()
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
            )
            
            // Draw ticks
            for (i in 0 until tickCount) {
                val angle = (i * 360f / tickCount - 90f) * PI.toFloat() / 180f
                
                // Calculate tick progress-based color
                val tickProgress = (i.toFloat() / tickCount)
                val progressForTick = (progress * tickCount - i) / 3f
                val colorProgress = progressForTick.coerceIn(0f, 1f)
                val tickColor = lerp(
                    Color(0xFF6E6E6E),
                    Color.Black,
                    colorProgress
                )
                
                // Calculate micro-movement offset (only when running)
                val motionAmplitude = if (isRunning) {
                    (sin(oscillation + i * 0.2f) * 1.5f * progress).coerceIn(-2f, 2f)
                } else 0f
                
                val perpAngle = angle + PI.toFloat() / 2
                val offsetX = cos(perpAngle) * motionAmplitude
                val offsetY = sin(perpAngle) * motionAmplitude
                
                // Calculate tick positions
                val x1 = centerX + cos(angle) * radiusInner + offsetX
                val y1 = centerY + sin(angle) * radiusInner + offsetY
                val x2 = centerX + cos(angle) * radiusOuter + offsetX
                val y2 = centerY + sin(angle) * radiusOuter + offsetY
                
                drawLine(
                    color = tickColor,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
        
        // Center text
        Text(
            text = timeText,
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E2E)
@Composable
fun InstantSessionsIdlePreview() {
    InstantSessions(
        onStart = {},
        onPause = {},
        onResume = {},
        onStop = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E2E)
@Composable
fun InstantSessionsRunningPreview() {
    var isRunning by remember { mutableStateOf(true) }
    var isPaused by remember { mutableStateOf(false) }
    var totalSeconds by remember { mutableStateOf(15 * 60) }
    var elapsedSeconds by remember { mutableStateOf(5 * 60) }

    LaunchedEffect(Unit) {
        while (isRunning && !isPaused && elapsedSeconds < totalSeconds) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    val progress = elapsedSeconds.toFloat() / totalSeconds.toFloat()

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

            RoundedSquareTimer(
                remainingSeconds = totalSeconds - elapsedSeconds,
                progress = progress,
                isRunning = !isPaused
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { isPaused = !isPaused },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isPaused) "Resume" else "Pause")
                }
                
                OutlinedButton(
                    onClick = { isRunning = false },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Stop", color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E2E)
@Composable
fun InstantSessionsPausedPreview() {
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

            RoundedSquareTimer(
                remainingSeconds = 153,
                progress = 0.66f,
                isRunning = false
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Resume")
                }
                
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Stop", color = Color.White)
                }
            }
        }
    }
}

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
