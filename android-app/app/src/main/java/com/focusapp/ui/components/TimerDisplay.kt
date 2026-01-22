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
import androidx.compose.ui.graphics.Path
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
                
                // Main timer display with pill/capsule progress
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(240.dp)
                        .height(100.dp)
                ) {
                    // Pill/capsule shape progress - filling the outline
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val strokeWidth = 8.dp.toPx()
                        val height = size.height
                        val width = size.width
                        val radius = height / 2
                        
                        // Background track (dark pill shape outline)
                        drawRoundRect(
                            color = Color(0xFF2A2A2A),
                            size = Size(width, height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                            style = Stroke(width = strokeWidth)
                        )
                        
                        // Progress fill inside the pill outline (no glow)
                        if (animatedProgress > 0) {
                            val path = Path()
                            val progressWidth = animatedProgress * width
                            
                            // Calculate the fill path based on progress
                            if (progressWidth <= radius) {
                                // Left cap only (circular segment)
                                val centerX = radius
                                val centerY = radius
                                val sweepAngle = Math.toDegrees(2 * Math.acos((radius - progressWidth) / radius.toDouble())).toFloat()
                                
                                path.moveTo(centerX, centerY - radius + strokeWidth)
                                path.arcTo(
                                    rect = androidx.compose.ui.geometry.Rect(
                                        left = strokeWidth,
                                        top = strokeWidth,
                                        right = 2 * radius - strokeWidth,
                                        bottom = height - strokeWidth
                                    ),
                                    startAngleDegrees = 270f - sweepAngle / 2,
                                    sweepAngleDegrees = sweepAngle,
                                    forceMoveTo = false
                                )
                            } else if (progressWidth >= width - radius) {
                                // Full width (both caps filled)
                                drawRoundRect(
                                    color = Color(0xFFCCFF00),
                                    topLeft = Offset(strokeWidth, strokeWidth),
                                    size = Size(width - 2 * strokeWidth, height - 2 * strokeWidth),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius - strokeWidth, radius - strokeWidth)
                                )
                            } else {
                                // Left cap + middle section
                                drawRoundRect(
                                    color = Color(0xFFCCFF00),
                                    topLeft = Offset(strokeWidth, strokeWidth),
                                    size = Size(progressWidth - strokeWidth, height - 2 * strokeWidth),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius - strokeWidth, radius - strokeWidth)
                                )
                            }
                            
                            if (path.isEmpty.not()) {
                                drawPath(
                                    path = path,
                                    color = Color(0xFFCCFF00)
                                )
                            }
                        }
                    }
                    
                    // Main time display - clean, crystal clear, properly sized and centered
                    Text(
                        text = String.format("%02d:%02d", remainingMinutes, remainingSecondsDisplay),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,  // White text for better contrast
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
                            
                            // Track with vertical line marks
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(horizontal = 24.dp)
                            ) {
                                val width = size.width
                                val height = size.height
                                val step = width / (durationOptions.size - 1)
                                val smallStep = step / 10f
                                
                                // Draw small light gray lines between main lines (10 per segment)
                                for (i in 0 until (durationOptions.size - 1)) {
                                    for (j in 1..9) {
                                        val x = i * step + j * smallStep
                                        drawLine(
                                            color = Color.LightGray.copy(alpha = 0.3f),
                                            start = Offset(x, height * 0.35f),
                                            end = Offset(x, height * 0.65f),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                    }
                                }
                                
                                // Draw main green vertical lines at discrete positions
                                durationOptions.forEachIndexed { index, _ ->
                                    val x = index * step
                                    val isSelected = index == selectedIndex
                                    
                                    drawLine(
                                        color = if (isSelected) Color(0xFFCCFF00) else Color(0xFFCCFF00).copy(alpha = 0.5f),
                                        start = Offset(x, height * 0.25f),
                                        end = Offset(x, height * 0.75f),
                                        strokeWidth = if (isSelected) 3.dp.toPx() else 2.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
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
