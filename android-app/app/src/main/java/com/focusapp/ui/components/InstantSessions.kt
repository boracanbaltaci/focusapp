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

// Helper function to calculate points along a rounded square path
private fun getRoundedSquarePoint(
    distance: Float,
    squareSize: Float,
    cornerRadius: Float,
    centerX: Float,
    centerY: Float
): Triple<Float, Float, Boolean> {
    val straightSide = squareSize - 2 * cornerRadius
    val quarterArc = PI.toFloat() * cornerRadius / 2
    val perimeter = 4 * straightSide + 4 * quarterArc
    
    var d = distance % perimeter
    val halfSquare = squareSize / 2
    val left = centerX - halfSquare
    val top = centerY - halfSquare
    val right = centerX + halfSquare
    val bottom = centerY + halfSquare
    
    var isCorner = false
    
    // Top edge (left to right, after top-left corner)
    if (d < straightSide) {
        val x = left + cornerRadius + d
        val y = top
        return Triple(x, y, false)
    }
    d -= straightSide
    
    // Top-right corner
    if (d < quarterArc) {
        val angle = -PI.toFloat() / 2 + d / cornerRadius
        val x = right - cornerRadius + cornerRadius * cos(angle)
        val y = top + cornerRadius + cornerRadius * sin(angle)
        isCorner = true
        return Triple(x, y, isCorner)
    }
    d -= quarterArc
    
    // Right edge (top to bottom)
    if (d < straightSide) {
        val x = right
        val y = top + cornerRadius + d
        return Triple(x, y, false)
    }
    d -= straightSide
    
    // Bottom-right corner
    if (d < quarterArc) {
        val angle = 0f + d / cornerRadius
        val x = right - cornerRadius + cornerRadius * cos(angle)
        val y = bottom - cornerRadius + cornerRadius * sin(angle)
        isCorner = true
        return Triple(x, y, isCorner)
    }
    d -= quarterArc
    
    // Bottom edge (right to left)
    if (d < straightSide) {
        val x = right - cornerRadius - d
        val y = bottom
        return Triple(x, y, false)
    }
    d -= straightSide
    
    // Bottom-left corner
    if (d < quarterArc) {
        val angle = PI.toFloat() / 2 + d / cornerRadius
        val x = left + cornerRadius + cornerRadius * cos(angle)
        val y = bottom - cornerRadius + cornerRadius * sin(angle)
        isCorner = true
        return Triple(x, y, isCorner)
    }
    d -= quarterArc
    
    // Left edge (bottom to top)
    if (d < straightSide) {
        val x = left
        val y = bottom - cornerRadius - d
        return Triple(x, y, false)
    }
    d -= straightSide
    
    // Top-left corner
    val angle = PI.toFloat() + d / cornerRadius
    val x = left + cornerRadius + cornerRadius * cos(angle)
    val y = top + cornerRadius + cornerRadius * sin(angle)
    isCorner = true
    return Triple(x, y, isCorner)
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
        modifier = Modifier.size(192.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val cornerRadius = 36.dp.toPx()
            val tickCount = 40 // Increased for better distribution around rounded square
            val centerX = size.width / 2
            val centerY = size.height / 2
            val squareSize = canvasSize - 32.dp.toPx()
            val halfSquare = squareSize / 2
            val tickInset = 12.dp.toPx()
            
            // Draw rounded square background
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(16.dp.toPx(), 16.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
            )
            
            // Draw ticks along rounded square path
            for (i in 0 until tickCount) {
                val t = i.toFloat() / tickCount
                val perimeter = 4 * (squareSize - 2 * cornerRadius) + 2 * PI.toFloat() * cornerRadius
                val distance = t * perimeter
                
                // Calculate position on rounded square path
                val (x, y, isCorner) = getRoundedSquarePoint(
                    distance = distance,
                    squareSize = squareSize,
                    cornerRadius = cornerRadius,
                    centerX = centerX,
                    centerY = centerY
                )
                
                // Calculate tick progress-based color
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
                
                // Calculate normal direction for tick
                val dx = x - centerX
                val dy = y - centerY
                val length = kotlin.math.sqrt(dx * dx + dy * dy)
                val normalX = dx / length
                val normalY = dy / length
                
                // Perpendicular for motion offset
                val perpX = -normalY * motionAmplitude
                val perpY = normalX * motionAmplitude
                
                // Tick length varies - shorter at corners
                val tickLength = if (isCorner) 12.dp.toPx() else 16.dp.toPx()
                
                // Calculate tick start and end positions
                val x1 = x - normalX * tickInset + perpX
                val y1 = y - normalY * tickInset + perpY
                val x2 = x - normalX * (tickInset + tickLength) + perpX
                val y2 = y - normalY * (tickInset + tickLength) + perpY
                
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
            fontSize = 48.sp
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
