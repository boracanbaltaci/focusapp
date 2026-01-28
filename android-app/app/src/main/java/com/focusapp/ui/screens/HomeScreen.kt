package com.focusapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
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
    var timerSeconds by remember { mutableStateOf(25 * 60) } // Default 25 minutes
    var showDurationPicker by remember { mutableStateOf(false) }
    
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
                        isTimerRunning = true
                    }
                },
                onTimerClick = { showDurationPicker = true },
                onNavigateToSettings = onNavigateToSettings
            )
        }
        
        // Duration picker dialog
        if (showDurationPicker) {
            DurationPickerDialog(
                onDismiss = { showDurationPicker = false },
                onDurationSelected = { seconds ->
                    timerSeconds = seconds
                    isTimerRunning = false
                    showDurationPicker = false
                }
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
                    ),
                    modifier = Modifier.widthIn(min = 420.dp) // Fixed minimum width to prevent jitter
                )
                
                if (period.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(1.dp))
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
    onTimerClick: () -> Unit,
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
            // Timer display with hour label - centered
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.clickable(enabled = !isRunning) { onTimerClick() }
            ) {
                // Show hour label if >= 1 hour
                val hours = seconds / 3600
                if (hours >= 1) {
                    val hourText = if (hours == 1) "1 hour" else "$hours hours"
                    Text(
                        text = hourText,
                        style = TextStyle(
                            fontFamily = MenilFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 70.sp,
                            color = Color.Black,
                            textAlign = TextAlign.End
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                
                Text(
                    text = formatTime(seconds),
                    style = TextStyle(
                        fontFamily = MenilFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 240.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.widthIn(min = 350.dp) // Fixed minimum width to prevent jitter
                )
            }
            
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
                    // Play or Pause icon using Canvas
                    Canvas(modifier = Modifier.size(24.dp)) {
                        if (isRunning) {
                            // Pause icon (two vertical lines)
                            val lineWidth = size.width * 0.15f
                            val lineHeight = size.height * 0.6f
                            val topOffset = (size.height - lineHeight) / 2f
                            
                            // Left line
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(size.width * 0.3f, topOffset),
                                size = Size(lineWidth, lineHeight)
                            )
                            
                            // Right line
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(size.width * 0.55f, topOffset),
                                size = Size(lineWidth, lineHeight)
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
            val radius = size.width * 0.4f
            val strokeWidth = 2.dp.toPx()
            
            // Draw a simple cog/settings icon with 6 teeth
            // Center circle
            drawCircle(
                color = Color.Black,
                radius = radius * 0.35f,
                center = Offset(centerX, centerY),
                style = Stroke(width = strokeWidth)
            )
            
            // Draw 6 rectangular teeth around the circle
            for (i in 0 until 6) {
                val angle = (i * 60f).toRadians()
                val toothLength = radius * 0.4f
                val toothWidth = radius * 0.25f
                
                val startX = centerX + (radius * 0.35f) * kotlin.math.cos(angle)
                val startY = centerY + (radius * 0.35f) * kotlin.math.sin(angle)
                val endX = centerX + (radius * 0.75f) * kotlin.math.cos(angle)
                val endY = centerY + (radius * 0.75f) * kotlin.math.sin(angle)
                
                // Draw tooth as a line
                drawLine(
                    color = Color.Black,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = toothWidth
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

@Composable
private fun DurationPickerDialog(
    onDismiss: () -> Unit,
    onDurationSelected: (Int) -> Unit
) {
    val durationOptions = listOf(
        Pair("5:00", 5 * 60),
        Pair("10:00", 10 * 60),
        Pair("15:00", 15 * 60),
        Pair("20:00", 20 * 60),
        Pair("30:00", 30 * 60),
        Pair("45:00", 45 * 60),
        Pair("1 hour 00:00", 60 * 60),
        Pair("1 hour 10:00", 70 * 60),
        Pair("1 hour 15:00", 75 * 60),
        Pair("1 hour 20:00", 80 * 60),
        Pair("1 hour 30:00", 90 * 60),
        Pair("1 hour 45:00", 105 * 60),
        Pair("2 hours 00:00", 120 * 60)
    )
    
    var selectedDuration by remember { mutableStateOf(25 * 60) } // Default 25 minutes
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(400.dp)
                .height(450.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Title
                Text(
                    text = "Select Duration",
                    style = TextStyle(
                        fontFamily = MenilFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    ),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp)
                )
                
                // Center selection rectangle
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(300.dp)
                        .height(60.dp)
                        .background(
                            color = Color(0xFFF0F0F0),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
                
                // Scrollable list of durations
                LazyColumn(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(300.dp)
                        .height(300.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Add spacers at top to allow scrolling items into center
                    item {
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                    
                    items(durationOptions.size) { index ->
                        val (label, seconds) = durationOptions[index]
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clickable { selectedDuration = seconds },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = TextStyle(
                                    fontFamily = MenilFontFamily,
                                    fontSize = 22.sp,
                                    color = Color.Black.copy(alpha = if (selectedDuration == seconds) 1f else 0.4f),
                                    fontWeight = if (selectedDuration == seconds) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                    
                    // Add spacers at bottom
                    item {
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
                
                // Checkmark button in bottom left corner
                IconButton(
                    onClick = { onDurationSelected(selectedDuration) },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, bottom = 24.dp)
                        .size(56.dp)
                ) {
                    Canvas(modifier = Modifier.size(32.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.2f, size.height * 0.5f)
                            lineTo(size.width * 0.4f, size.height * 0.7f)
                            lineTo(size.width * 0.8f, size.height * 0.2f)
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF4CAF50),
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
    }
}
