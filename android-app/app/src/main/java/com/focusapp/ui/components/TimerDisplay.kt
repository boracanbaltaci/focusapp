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
    var elapsedMillis by remember { mutableStateOf(0L) } // For smooth continuous animation
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
    
    // Timer countdown logic with millisecond precision for smooth animation
    LaunchedEffect(isRunning) {
        val startTime = System.currentTimeMillis() - elapsedMillis
        while (isRunning) {
            val currentTime = System.currentTimeMillis()
            elapsedMillis = currentTime - startTime
            elapsedSeconds = (elapsedMillis / 1000).toInt()
            
            if (elapsedSeconds >= duration) {
                isRunning = false
                elapsedMillis = duration * 1000L
                elapsedSeconds = duration
            }
            
            delay(16L) // ~60fps for smooth animation
        }
    }
    
    // Smooth continuous progress (not stepwise)
    val progress = if (duration > 0) {
        val totalMillis = duration * 1000L
        (elapsedMillis.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
    } else 0f
    
    val remainingSeconds = duration - elapsedSeconds
    val remainingMinutes = remainingSeconds / 60
    val remainingSecondsDisplay = remainingSeconds % 60
    
    // Use progress directly for continuous smooth animation (no extra animation layer)
    val animatedProgress = progress
    
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
                    modifier = Modifier
                        .size(200.dp)
                ) {
                    // Circular progress - smooth continuous animation
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val strokeWidth = 12.dp.toPx()
                        val diameter = size.minDimension
                        val radius = diameter / 2
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        
                        // Background track (dark circle outline)
                        drawCircle(
                            color = Color(0xFF2A2A2A),
                            radius = radius - strokeWidth / 2,
                            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                            style = Stroke(width = strokeWidth)
                        )
                        
                        // Progress arc - starts from top center (-90°), progresses clockwise
                        if (animatedProgress > 0) {
                            drawArc(
                                color = Color(0xFFCCFF00), // Bright green
                                startAngle = -90f,
                                sweepAngle = animatedProgress * 360f,
                                useCenter = false,
                                topLeft = androidx.compose.ui.geometry.Offset(
                                    centerX - radius + strokeWidth / 2,
                                    centerY - radius + strokeWidth / 2
                                ),
                                size = Size(
                                    (radius - strokeWidth / 2) * 2,
                                    (radius - strokeWidth / 2) * 2
                                ),
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            )
                        }
                    }
                    
                    // Main time display - clean, crystal clear, properly sized and centered
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = String.format("%02d:%02d", remainingMinutes, remainingSecondsDisplay),
                            fontSize = 36.sp, // Slightly reduced for better fit (was 42sp)
                            fontWeight = FontWeight.Bold,
                            color = Color.White,  // White text for better contrast
                            style = MaterialTheme.typography.displayLarge,
                            textAlign = TextAlign.Center
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
                            elapsedMillis = 0L
                        } else {
                            isRunning = true
                            elapsedSeconds = 0
                            elapsedMillis = 0L
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
