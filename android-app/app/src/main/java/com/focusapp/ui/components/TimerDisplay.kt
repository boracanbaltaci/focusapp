package com.focusapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun TimerDisplay(
    modifier: Modifier = Modifier
) {
    var duration by remember { mutableStateOf(15 * 60) } // 15 minutes default
    var elapsedSeconds by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    
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
    
    // Animated progress for smooth transitions
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300)
    )
    
    // Glow animation
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
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
                    text = String.format("%02d:00", duration / 60),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 16.sp
                )
                
                // Main timer display with circular progress
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(200.dp)
                ) {
                    // Circular progress ring
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val strokeWidth = 12.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val center = Offset(size.width / 2, size.height / 2)
                        
                        // Background track
                        drawCircle(
                            color = Color(0xFF2A2A2A),
                            radius = radius,
                            center = center,
                            style = Stroke(width = strokeWidth)
                        )
                        
                        // Progress arc with glow
                        val sweepAngle = animatedProgress * 360f
                        if (sweepAngle > 0) {
                            // Draw glow layer
                            drawArc(
                                color = Color(0xFFCCFF00).copy(alpha = glowAlpha * 0.3f),
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
                            
                            // Main progress arc
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0xFFCCFF00),
                                        Color(0xFF99CC00),
                                        Color(0xFFCCFF00)
                                    )
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
                        }
                    }
                    
                    // Main time display with glow
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Glow layer (blurred background)
                        Text(
                            text = String.format("%02d:%02d", remainingMinutes, remainingSecondsDisplay),
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCCFF00).copy(alpha = glowAlpha * 0.5f),
                            modifier = Modifier.blur(16.dp)
                        )
                        
                        // Main text
                        Text(
                            text = String.format("%02d:%02d", remainingMinutes, remainingSecondsDisplay),
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCCFF00),
                            modifier = Modifier.offset(y = (-56).sp.value.dp)
                        )
                    }
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
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Minus and Plus buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Minus button
                    IconButton(
                        onClick = {
                            if (!isRunning && duration > 60) {
                                duration -= 60 // Decrease by 1 minute
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF2A2A2A), shape = CircleShape)
                    ) {
                        Text(
                            text = "−",
                            fontSize = 24.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Plus button
                    IconButton(
                        onClick = {
                            if (!isRunning && duration < 3600) {
                                duration += 60 // Increase by 1 minute
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF2A2A2A), shape = CircleShape)
                    ) {
                        Text(
                            text = "+",
                            fontSize = 24.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
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
