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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun TimerDisplay(
    modifier: Modifier = Modifier
) {
    // Discrete duration values in minutes
    val durationOptions = listOf(5, 10, 15, 30, 45, 60)
    var selectedIndex by remember { mutableStateOf(2) } // Default to 15 minutes
    var duration by remember { mutableStateOf(durationOptions[selectedIndex] * 60) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(selectedIndex.toFloat()) }
    
    // Animated slider position for smooth snapping
    val animatedSliderPosition by animateFloatAsState(
        targetValue = sliderPosition,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
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
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
    
    // Bright light glow pulse animation (not liquid - modern light effect)
    val infiniteTransition = rememberInfiniteTransition()
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
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
                    text = "${durationOptions[selectedIndex]}:00",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 16.sp
                )
                
                // Main timer display with circular progress
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(200.dp)
                ) {
                    // Circular progress ring with bright modern light effect
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val strokeWidth = 12.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val center = Offset(size.width / 2, size.height / 2)
                        
                        // Background track (dark)
                        drawCircle(
                            color = Color(0xFF2A2A2A),
                            radius = radius,
                            center = center,
                            style = Stroke(width = strokeWidth)
                        )
                        
                        // Progress arc with bright light effect
                        val sweepAngle = animatedProgress * 360f
                        if (sweepAngle > 0) {
                            // Outer bright glow (yellow/white light)
                            drawArc(
                                color = Color(0xFFFFFF00).copy(alpha = 0.25f * glowPulse),
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth + 20.dp.toPx(), cap = StrokeCap.Round),
                                topLeft = Offset(
                                    center.x - radius,
                                    center.y - radius
                                ),
                                size = Size(radius * 2, radius * 2)
                            )
                            
                            // Mid bright glow
                            drawArc(
                                color = Color(0xFFFFFF00).copy(alpha = 0.5f * glowPulse),
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth + 10.dp.toPx(), cap = StrokeCap.Round),
                                topLeft = Offset(
                                    center.x - radius,
                                    center.y - radius
                                ),
                                size = Size(radius * 2, radius * 2)
                            )
                            
                            // Main progress arc (bright yellow-green)
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0xFFCCFF00),
                                        Color(0xFFFFFF00),
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
                        }
                    }
                    
                    // Main time display - clean, crystal clear, properly sized and centered
                    Text(
                        text = String.format("%02d:%02d", remainingMinutes, remainingSecondsDisplay),
                        fontSize = 42.sp,  // Reduced from 56sp to fit better in circle
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCCFF00),  // Match the bright yellow-green theme
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
                
                // Interactive discrete horizontal slider for duration adjustment
                if (!isRunning) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(60.dp)
                        ) {
                            // Background track
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Color(0xFF2A2A2A),
                                        shape = RoundedCornerShape(30.dp)
                                    )
                                    .pointerInput(durationOptions) {
                                        detectDragGestures(
                                            onDragEnd = {
                                                // Snap to nearest position
                                                val nearest = sliderPosition.roundToInt().coerceIn(0, durationOptions.size - 1)
                                                sliderPosition = nearest.toFloat()
                                                selectedIndex = nearest
                                                duration = durationOptions[selectedIndex] * 60
                                            }
                                        ) { change, dragAmount ->
                                            change.consume()
                                            val maxWidth = size.width.toFloat()
                                            val step = maxWidth / (durationOptions.size - 1)
                                            
                                            // Update slider position
                                            sliderPosition = ((sliderPosition * step) + dragAmount.x) / step
                                            sliderPosition = sliderPosition.coerceIn(0f, (durationOptions.size - 1).toFloat())
                                            
                                            // Update duration in real-time (for preview)
                                            val currentIndex = sliderPosition.roundToInt().coerceIn(0, durationOptions.size - 1)
                                            duration = durationOptions[currentIndex] * 60
                                        }
                                    }
                            )
                            
                            // Track with discrete marks
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center)
                                    .padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                durationOptions.forEachIndexed { index, _ ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (index == selectedIndex) 10.dp else 6.dp)
                                            .background(
                                                if (index == selectedIndex) 
                                                    Color(0xFFCCFF00) 
                                                else 
                                                    Color.White.copy(alpha = 0.3f),
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                            
                            // Current duration display (centered)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${durationOptions[selectedIndex]}m",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Duration labels
                        Row(
                            modifier = Modifier.fillMaxWidth(0.85f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            durationOptions.forEach { minutes ->
                                Text(
                                    text = "${minutes}m",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.width(30.dp),
                                    textAlign = TextAlign.Center
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
