package com.focusapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusapp.ui.theme.MenilFontFamily
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun HomeScreen(
    sessionViewModel: SessionViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToSettings: () -> Unit
) {
    // Background color - off white
    val backgroundColor = Color(0xFFFBFBFB)
    
    // Navigation state (1 = middle screen with clock, 2 = timer screen)
    var currentScreen by remember { mutableStateOf(1) }
    
    // Swipe gesture state
    var offsetX by remember { mutableStateOf(0f) }
    
    // Real-time clock
    var currentTime by remember { mutableStateOf(getCurrentTimeString()) }
    
    // Timer state
    var isTimerRunning by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(0) }
    
    // Update clock every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = getCurrentTimeString()
            delay(1000)
        }
    }
    
    // Timer countdown
    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && timerSeconds > 0) {
            delay(1000)
            timerSeconds--
        }
        if (timerSeconds == 0) {
            isTimerRunning = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            offsetX > 100 && currentScreen > 0 -> currentScreen--
                            offsetX < -100 && currentScreen < 2 -> currentScreen++
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount
                    }
                )
            }
    ) {
        // Decorative arcs in background
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val centerX = size.width / 2f
            
            // Upper arc
            val upperCenterY = size.height * 0.45f
            val upperRadius = size.width * 0.65f
            
            drawArc(
                color = Color(0xFFE5E5E5),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(centerX - upperRadius, upperCenterY - upperRadius),
                size = androidx.compose.ui.geometry.Size(upperRadius * 2, upperRadius * 2),
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Middle arc
            val middleCenterY = size.height * 0.5f
            val middleRadius = size.width * 0.55f
            
            drawArc(
                color = Color(0xFFE5E5E5),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(centerX - middleRadius, middleCenterY - middleRadius),
                size = androidx.compose.ui.geometry.Size(middleRadius * 2, middleRadius * 2),
                style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Screen content based on current screen
        when (currentScreen) {
            0 -> PlaceholderScreen(onNavigateToSettings)
            1 -> ClockScreen(currentTime, onNavigateToSettings)
            2 -> TimerScreen(
                isRunning = isTimerRunning,
                seconds = timerSeconds,
                onStartStop = { 
                    if (isTimerRunning) {
                        isTimerRunning = false
                    } else {
                        if (timerSeconds == 0) timerSeconds = 60 // Default 1 minute
                        isTimerRunning = true
                    }
                },
                onNavigateToSettings = onNavigateToSettings
            )
        }
        
        // Bottom navigation dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                NavigationDot(
                    isActive = index == currentScreen,
                    onClick = { currentScreen = index }
                )
                if (index < 2) {
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(onNavigateToSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top bar with settings icon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, end = 24.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            SettingsIconButton(onNavigateToSettings)
        }
        
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Screen 1",
                style = TextStyle(
                    fontFamily = MenilFontFamily,
                    fontSize = 48.sp,
                    color = Color.Black.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun ClockScreen(currentTime: String, onNavigateToSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top bar with settings icon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, end = 24.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            SettingsIconButton(onNavigateToSettings)
        }
        
        // Center clock display
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            val timeParts = currentTime.split("\n")
            val time = timeParts[0]
            val period = if (timeParts.size > 1) timeParts[1] else ""
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = time,
                    style = TextStyle(
                        fontFamily = MenilFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 240.sp,
                        lineHeight = 240.sp,
                        letterSpacing = 2.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                )
                
                if (period.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = period,
                        style = TextStyle(
                            fontFamily = MenilFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 75.sp,
                            lineHeight = 75.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun TimerScreen(
    isRunning: Boolean,
    seconds: Int,
    onStartStop: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top bar with settings icon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, end = 24.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            SettingsIconButton(onNavigateToSettings)
        }
        
        // Center content with timer centered and button on far left
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Timer display - centered
            Text(
                text = formatTime(seconds),
                style = TextStyle(
                    fontFamily = MenilFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 240.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            )
            
            // Start/Stop button on the far left
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                Button(
                    onClick = onStartStop,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) Color(0xFFFF4444) else Color(0xFF4CAF50)
                    ),
                    modifier = Modifier
                        .padding(start = 24.dp)
                        .size(56.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    // Play or Stop icon using Canvas
                    Canvas(modifier = Modifier.size(24.dp)) {
                        if (isRunning) {
                            // Stop icon (square)
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(size.width * 0.25f, size.height * 0.25f),
                                size = Size(size.width * 0.5f, size.height * 0.5f)
                            )
                        } else {
                            // Play icon (triangle)
                            val path = Path().apply {
                                moveTo(size.width * 0.3f, size.height * 0.2f)
                                lineTo(size.width * 0.3f, size.height * 0.8f)
                                lineTo(size.width * 0.75f, size.height * 0.5f)
                                close()
                            }
                            drawPath(
                                path = path,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun SettingsIconButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(32.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = size.width * 0.35f
            val strokeWidth = 2.dp.toPx()
            
            // Draw gear icon (simplified version)
            // Center circle
            drawCircle(
                color = Color.Black,
                radius = radius * 0.4f,
                center = Offset(centerX, centerY),
                style = Stroke(width = strokeWidth)
            )
            
            // Outer gear teeth (8 small circles around)
            for (i in 0 until 8) {
                val angle = (i * 45f).toRadians()
                val x = centerX + radius * kotlin.math.cos(angle)
                val y = centerY + radius * kotlin.math.sin(angle)
                drawCircle(
                    color = Color.Black,
                    radius = radius * 0.15f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

private fun Float.toRadians(): Float = this * Math.PI.toFloat() / 180f

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

@Composable
private fun NavigationDot(
    isActive: Boolean,
    onClick: () -> Unit
) {
    val size = if (isActive) 12.dp else 8.dp
    val color = if (isActive) Color(0xFF000000) else Color(0xFF545454)
    
    Box(
        modifier = Modifier
            .size(size)
            .background(color, shape = androidx.compose.foundation.shape.CircleShape)
            .clickable(onClick = onClick)
    )
}

private fun getCurrentTimeString(): String {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    
    // Get locale to determine AM/PM or ÖÖ/ÖS
    val locale = Locale.getDefault()
    val isTurkish = locale.language == "tr"
    
    return if (isTurkish) {
        // Turkish format with ÖÖ/ÖS
        val period = if (hour < 12) "öö" else "ös"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        String.format("%02d:%02d\n%s", displayHour, minute, period)
    } else {
        // English format with AM/PM
        val period = if (hour < 12) "am" else "pm"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        String.format("%02d:%02d\n%s", displayHour, minute, period)
    }
}
