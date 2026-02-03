package com.focusapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.focusapp.R
import com.focusapp.data.StatisticsRepository
import com.focusapp.ui.theme.MenilFontFamily
import com.focusapp.ui.theme.AvocadoFontFamily
import com.focusapp.ui.theme.GeistFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    sessionViewModel: SessionViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val statisticsRepository = remember { StatisticsRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // Collect settings
    val clockFont by settingsViewModel.clockFont.collectAsState()
    val theme by settingsViewModel.theme.collectAsState()
    
    // Theme-based colors
    val backgroundColor = if (theme == "dark") Color(0xFF181C14) else Color(0xFFFBFBFB)
    val textColor = if (theme == "dark") Color(0xFFECDFCC) else Color.Black
    val arcColor = if (theme == "dark") Color(0xFFECDFCC).copy(alpha = 0.3f) else Color(0xFFE5E5E5)
    
    // Font selection
    val clockFontFamily = if (clockFont == "avocado") AvocadoFontFamily else MenilFontFamily
    
    // Navigation state (1 = middle screen with clock, 2 = timer screen)
    var currentScreen by remember { mutableStateOf(1) }
    
    // Swipe gesture state
    var offsetX by remember { mutableStateOf(0f) }
    
    // Real-time clock
    var currentTime by remember { mutableStateOf(getCurrentTimeString()) }
    
    // Timer state
    var isTimerRunning by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(25 * 60) } // Default 25 minutes
    var initialTimerSeconds by remember { mutableStateOf(25 * 60) } // Track initial duration
    var showDurationPicker by remember { mutableStateOf(false) }
    var timerSessionId by remember { mutableStateOf<Long?>(null) } // Track session ID for backend sync
    
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
            // Complete the session and sync to backend
            timerSessionId?.let { sessionId ->
                sessionViewModel.endSession()
            }
            // Also save to legacy statistics for compatibility
            val durationMinutes = initialTimerSeconds / 60
            statisticsRepository.saveSession(durationMinutes)
            timerSessionId = null
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
                color = arcColor,
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
                color = arcColor,
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
            0 -> StatisticsScreen(onNavigateToSettings, textColor)
            1 -> ClockScreen(currentTime, onNavigateToSettings, clockFontFamily, textColor)
            2 -> TimerScreen(
                isRunning = isTimerRunning,
                seconds = timerSeconds,
                onStartStop = { 
                    if (isTimerRunning) {
                        // Stopping timer - end the session and sync to backend
                        isTimerRunning = false
                        timerSessionId?.let { sessionId ->
                            sessionViewModel.endSession()
                        }
                        timerSessionId = null
                    } else {
                        // Starting timer - start a new session
                        initialTimerSeconds = timerSeconds
                        sessionViewModel.startSession(isBreak = false)
                        // Store the session ID after it's created
                        coroutineScope.launch {
                            delay(100) // Wait for session to be created
                            timerSessionId = sessionViewModel.currentSession.value?.id
                        }
                        isTimerRunning = true
                    }
                },
                onTimerClick = { 
                    if (!isTimerRunning) {
                        showDurationPicker = true 
                    }
                },
                onFinish = {
                    // Finish/end the session via "bitir" button and sync to backend
                    isTimerRunning = false
                    timerSessionId?.let { sessionId ->
                        sessionViewModel.endSession()
                    }
                    // Also save to legacy statistics for compatibility
                    val durationMinutes = (initialTimerSeconds - timerSeconds) / 60
                    if (durationMinutes > 0) {
                        statisticsRepository.saveSession(durationMinutes)
                    }
                    // Reset to initial duration
                    timerSeconds = initialTimerSeconds
                    timerSessionId = null
                },
                onNavigateToSettings = onNavigateToSettings,
                clockFontFamily = clockFontFamily,
                textColor = textColor
            )
        }
        
        // Duration picker dialog
        if (showDurationPicker) {
            DurationPickerDialog(
                onDismiss = { showDurationPicker = false },
                onDurationSelected = { seconds ->
                    timerSeconds = seconds
                    initialTimerSeconds = seconds // Track initial duration for statistics
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
private fun ClockScreen(
    currentTime: String,
    onNavigateToSettings: () -> Unit,
    clockFontFamily: FontFamily,
    textColor: Color
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Settings icon with absolute positioning (7% from top, 7% from right)
        SettingsIconButton(onNavigateToSettings, textColor)
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Center clock display - perfectly centered
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
                        fontFamily = clockFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 240.sp,
                        lineHeight = 240.sp,
                        letterSpacing = 2.sp,
                        color = textColor,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.widthIn(min = 300.dp) // Fixed minimum width to prevent jitter
                )
                
                if (period.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(0.5.dp)) // Reduced spacing for closer proximity
                    Text(
                        text = period,
                        style = TextStyle(
                            fontFamily = clockFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 75.sp,
                            lineHeight = 75.sp,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TimerScreen(
    isRunning: Boolean,
    seconds: Int,
    onStartStop: () -> Unit,
    onTimerClick: () -> Unit,
    onFinish: () -> Unit,
    onNavigateToSettings: () -> Unit,
    clockFontFamily: FontFamily,
    textColor: Color
) {
    val context = LocalContext.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Settings icon with absolute positioning (7% from top, 7% from right)
        SettingsIconButton(onNavigateToSettings, textColor)
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Center content with timer centered and button on far left
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Timer display with hour label - centered
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.clickable(enabled = !isRunning) { onTimerClick() }
                ) {
                // Calculate remaining time after removing full hours
                val totalMinutes = seconds / 60
                val hours = totalMinutes / 60
                val remainingMinutes = totalMinutes % 60
                val remainingSeconds = seconds % 60
                
                // Show hour label if >= 1 hour (60 minutes or more)
                if (totalMinutes >= 60) {
                    val hourText = if (hours == 1) {
                        "1 " + context.getString(R.string.hour_singular)
                    } else {
                        "$hours " + context.getString(R.string.hours_plural)
                    }
                    Text(
                        text = hourText,
                        style = TextStyle(
                            fontFamily = clockFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 70.sp,
                            color = textColor,
                            textAlign = TextAlign.End
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                
                // Display time: if >= 60 minutes, show remaining time after hours
                // if < 60 minutes, show full time
                val displayTime = if (totalMinutes >= 60) {
                    String.format("%02d:%02d", remainingMinutes, remainingSeconds)
                } else {
                    String.format("%02d:%02d", totalMinutes, remainingSeconds)
                }
                
                Text(
                    text = displayTime,
                    style = TextStyle(
                        fontFamily = clockFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 240.sp,
                        color = textColor,
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
            
            // Finish button on the far right (always visible)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(
                    onClick = onFinish,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9E9E9E) // Gray color for finish
                    ),
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .size(56.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    // Stop/Finish icon (square) using Canvas
                    Canvas(modifier = Modifier.size(20.dp)) {
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(0f, 0f),
                            size = Size(size.width, size.height)
                        )
                    }
                }
            }
        }
        
        
        Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SettingsIconButton(onClick: () -> Unit, iconColor: Color) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Position: 7% from top, 7% from right (93% from left)
        val xOffset = maxWidth * 0.93f - 16.dp  // 7% from right
        val yOffset = maxHeight * 0.07f          // 7% from top
        
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(32.dp)
                .offset(x = xOffset, y = yOffset)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val radius = size.width * 0.4f
                val strokeWidth = 2.dp.toPx()
                
                // Draw a simple cog/settings icon with 6 teeth
                // Center circle
                drawCircle(
                    color = iconColor,
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
                        color = iconColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = toothWidth
                    )
                }
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
    
    // Use standard AM/PM
    val period = if (hour < 12) "am" else "pm"
    
    val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    return String.format("%02d:%02d\n%s", displayHour, minute, period)
}

@Composable
private fun DurationPickerDialog(
    onDismiss: () -> Unit,
    onDurationSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val hourStr = stringResource(R.string.hour_singular)
    val hoursStr = stringResource(R.string.hours_plural)
    
    val durationOptions = listOf(
        Pair("5:00", 5 * 60),
        Pair("10:00", 10 * 60),
        Pair("15:00", 15 * 60),
        Pair("20:00", 20 * 60),
        Pair("30:00", 30 * 60),
        Pair("45:00", 45 * 60),
        Pair("1 $hourStr", 60 * 60),
        Pair("1 $hourStr 10", 70 * 60),
        Pair("1 $hourStr 15", 75 * 60),
        Pair("1 $hourStr 20", 80 * 60),
        Pair("1 $hourStr 30", 90 * 60),
        Pair("1 $hourStr 45", 105 * 60),
        Pair("2 $hoursStr", 120 * 60)
    )
    
    // Simple state to track selected index - default to 5:00 (index 0)
    var selectedIndex by remember { mutableStateOf(0) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(350.dp)
                .height(500.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Title
                Text(
                    text = stringResource(R.string.select_duration),
                    style = TextStyle(
                        fontFamily = GeistFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
                
                // Scrollable list of clickable durations
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(durationOptions.size) { index ->
                        val (label, seconds) = durationOptions[index]
                        val isSelected = selectedIndex == index
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(
                                    color = if (isSelected) Color(0xFFE8F5E9) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedIndex = index
                                }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                style = TextStyle(
                                    fontFamily = GeistFontFamily,
                                    fontSize = 20.sp,
                                    color = Color.Black,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            
                            // Show checkmark for selected item
                            if (isSelected) {
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    val path = Path().apply {
                                        moveTo(size.width * 0.2f, size.height * 0.5f)
                                        lineTo(size.width * 0.4f, size.height * 0.7f)
                                        lineTo(size.width * 0.8f, size.height * 0.2f)
                                    }
                                    drawPath(
                                        path = path,
                                        color = Color(0xFF4CAF50),
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Confirm button at bottom
                Button(
                    onClick = {
                        val selectedDuration = durationOptions[selectedIndex].second
                        onDurationSelected(selectedDuration)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.confirm),
                        style = TextStyle(
                            fontFamily = GeistFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}
