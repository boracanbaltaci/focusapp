package com.focusapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun TimerDisplay(
    modifier: Modifier = Modifier
) {
    var duration by remember { mutableStateOf(15 * 60) } // 15 minutes default
    var elapsedSeconds by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var sliderOffset by remember { mutableStateOf(0f) }
    
    // Timer countdown logic
    LaunchedEffect(isRunning, elapsedSeconds) {
        if (isRunning && elapsedSeconds < duration) {
            delay(1000L)
            elapsedSeconds++
        } else if (isRunning && elapsedSeconds >= duration) {
            isRunning = false
        }
    }
    
    val progress = if (duration > 0) elapsedSeconds.toFloat() / duration.toFloat() else 0f
    val remainingSeconds = duration - elapsedSeconds
    val remainingMinutes = remainingSeconds / 60
    val remainingSecondsDisplay = remainingSeconds % 60
    
    // Animated progress for smooth, liquid-like transitions
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    // Liquid wave animation for realistic effect
    val infiniteTransition = rememberInfiniteTransition()
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    // Subtle glow pulse for liquid effect
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Format duration for display
    fun formatDuration(seconds: Int): String {
        val hours = seconds / 3600
        val mins = (seconds % 3600) / 60
        return when {
            hours == 0 -> "${mins}m"
            mins == 0 -> "${hours}h"
            else -> "${hours}h ${mins}m"
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, shape = RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Timer label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⏱️",
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Timer",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Duration text (small text above timer)
                Text(
                    text = formatDuration(duration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 16.sp
                )
                
                // Main timer display with circular progress
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(200.dp)
                ) {
                    // Circular progress ring with liquid-like realistic effect
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val strokeWidth = 14.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val center = Offset(size.width / 2, size.height / 2)
                        
                        // Outer shadow layer (realistic depth)
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.3f),
                            radius = radius + 4.dp.toPx(),
                            center = center.copy(y = center.y + 2.dp.toPx())
                        )
                        
                        // Inner shadow layer
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.5f),
                            radius = radius - strokeWidth / 2,
                            center = center
                        )
                        
                        // Background track with depth
                        drawCircle(
                            color = Color(0xFF2A2A2A),
                            radius = radius,
                            center = center,
                            style = Stroke(width = strokeWidth)
                        )
                        
                        // Inner highlight for depth
                        drawCircle(
                            color = Color.White.copy(alpha = 0.05f),
                            radius = radius,
                            center = center.copy(y = center.y - 1.dp.toPx()),
                            style = Stroke(width = strokeWidth * 0.3f)
                        )
                        
                        // Progress arc with liquid effect
                        val sweepAngle = animatedProgress * 360f
                        if (sweepAngle > 0) {
                            // Outer glow (soft, realistic)
                            drawArc(
                                color = Color(0xFFCCFF00).copy(alpha = 0.15f * glowPulse),
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth + 16.dp.toPx(), cap = StrokeCap.Round),
                                topLeft = Offset(
                                    center.x - radius,
                                    center.y - radius
                                ),
                                size = Size(radius * 2, radius * 2)
                            )
                            
                            // Mid glow layer
                            drawArc(
                                color = Color(0xFFCCFF00).copy(alpha = 0.3f * glowPulse),
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth + 8.dp.toPx(), cap = StrokeCap.Round),
                                topLeft = Offset(
                                    center.x - radius,
                                    center.y - radius
                                ),
                                size = Size(radius * 2, radius * 2)
                            )
                            
                            // Main progress arc with gradient (liquid look)
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0xFFCCFF00),
                                        Color(0xFF99CC00),
                                        Color(0xFF66AA00),
                                        Color(0xFF99CC00),
                                        Color(0xFFCCFF00)
                                    ),
                                    center = center
                                ),
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                topLeft = Offset(
                                    center.x - radius,
                                    center.y - radius
                                ),
                                size = Size(radius * 2, radius * 2)
                            )
                            
                            // Top highlight (glossy liquid effect)
                            drawArc(
                                color = Color.White.copy(alpha = 0.4f * glowPulse),
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth * 0.3f, cap = StrokeCap.Round),
                                topLeft = Offset(
                                    center.x - radius,
                                    center.y - radius - 1.dp.toPx()
                                ),
                                size = Size(radius * 2, radius * 2)
                            )
                            
                            // Bottom shadow (depth)
                            drawArc(
                                color = Color.Black.copy(alpha = 0.3f),
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth * 0.3f, cap = StrokeCap.Round),
                                topLeft = Offset(
                                    center.x - radius,
                                    center.y - radius + 2.dp.toPx()
                                ),
                                size = Size(radius * 2, radius * 2)
                            )
                        }
                    }
                    
                    // Main time display - clean, crystal clear, no glow
                    Text(
                        text = String.format("%02d:%02d", remainingMinutes, remainingSecondsDisplay),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = MaterialTheme.typography.displayLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Stop button
                Button(
                    onClick = {
                        if (isRunning) {
                            isRunning = false
                            elapsedSeconds = 0
                        } else {
                            isRunning = true
                            elapsedSeconds = 0
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2A2A)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(120.dp)
                        .height(40.dp)
                ) {
                    Text(
                        text = if (isRunning) "Stop" else "Start",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Horizontal slider for duration adjustment
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Slider track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(50.dp)
                            .background(
                                Color(0xFF2A2A2A),
                                shape = RoundedCornerShape(25.dp)
                            )
                            .pointerInput(Unit) {
                                if (!isRunning) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        sliderOffset += dragAmount.x
                                        
                                        // Calculate duration based on drag (1 min to 2 hours)
                                        val maxWidth = size.width.toFloat()
                                        val normalizedOffset = (sliderOffset / maxWidth).coerceIn(0f, 1f)
                                        
                                        // Map to 1-120 minutes (1 min to 2 hours)
                                        val totalMinutes = (1 + (normalizedOffset * 119)).roundToInt()
                                        duration = totalMinutes * 60
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Current duration display in slider
                        Text(
                            text = formatDuration(duration),
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        // Left and right indicators
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "◀",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "▶",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Helper text
                    Text(
                        text = "← Drag to adjust duration →",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E2E)
@Composable
fun TimerDisplayPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E1E2E))
                .padding(16.dp)
        ) {
            TimerDisplay()
        }
    }
}
