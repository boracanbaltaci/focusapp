package com.focusapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.focusapp.ui.components.ProportionalScaleBox
import com.focusapp.ui.theme.MenilFontFamily
import com.focusapp.ui.theme.AvocadoFontFamily
import com.focusapp.ui.theme.BreakFontFamily
import com.focusapp.ui.theme.DxburstFontFamily
import com.focusapp.ui.theme.KiyaFontFamily
import com.focusapp.ui.theme.FlaviotteFontFamily
import com.focusapp.ui.theme.AwesomeWaysFontFamily
import com.focusapp.ui.theme.TeheganFontFamily
import com.focusapp.ui.theme.WonderiaFontFamily
import com.focusapp.ui.theme.Kino40FontFamily
import com.focusapp.ui.theme.Font1797FontFamily
import com.focusapp.ui.theme.GlinaFontFamily
import com.focusapp.ui.theme.SentientFontFamily
import com.focusapp.ui.theme.ChillaxFontFamily
import com.focusapp.ui.theme.GeistFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    sessionViewModel: SessionViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val statisticsRepository = remember { StatisticsRepository(context) }
    
    // Collect settings
    val clockFont by settingsViewModel.clockFont.collectAsState()
    val theme by settingsViewModel.theme.collectAsState()
    
    // Theme-based colors
    val backgroundColor = if (theme == "dark") Color(0xFF181C14) else Color(0xFFFBFBFB)
    val textColor = if (theme == "dark") Color(0xFFECDFCC) else Color.Black
    val arcColor = if (theme == "dark") Color(0xFFECDFCC).copy(alpha = 0.3f) else Color(0xFFE5E5E5)
    
    // Font selection
    val clockFontFamily = when (clockFont) {
        "avocado" -> AvocadoFontFamily
        "break" -> BreakFontFamily
        "dxburst" -> DxburstFontFamily
        "kiya" -> KiyaFontFamily
        "flaviotte" -> FlaviotteFontFamily
        "awesome" -> AwesomeWaysFontFamily
        "tehegan" -> TeheganFontFamily
        "wonderia" -> WonderiaFontFamily
        "kino40" -> Kino40FontFamily
        "1797" -> Font1797FontFamily
        "glina" -> GlinaFontFamily
        "sentient" -> SentientFontFamily
        "chillax" -> ChillaxFontFamily
        else -> MenilFontFamily
    }
    
    // Pager state (1 = middle screen with clock)
    // 0 = Statistics, 1 = Clock, 2 = Timer
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    
    // Real-time clock
    val amString = stringResource(R.string.am)
    val pmString = stringResource(R.string.pm)
    var currentTime by remember { mutableStateOf(getCurrentTimeString(amString, pmString)) }
    
    // Timer state
    var isTimerRunning by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(25 * 60) } // Default 25 minutes
    var initialTimerSeconds by remember { mutableStateOf(25 * 60) } // Track initial duration
    var showDurationPicker by remember { mutableStateOf(false) }
    var isOnBreak by remember { mutableStateOf(false) } // Track break state
    var timerGeneration by remember { mutableStateOf(0) } // Force LaunchedEffect restart
    
    // Auto break settings
    val autoBreakEnabled by settingsViewModel.autoBreakEnabled.collectAsState()
    val breakDurationMinutes by settingsViewModel.breakDurationMinutes.collectAsState()
    val is24HourFormat by settingsViewModel.is24HourFormat.collectAsState()
    
    // Update clock every second
    LaunchedEffect(amString, pmString, is24HourFormat) {
        while (true) {
            currentTime = getCurrentTimeString(amString, pmString, is24HourFormat)
            delay(1000)
        }
    }
    
    // Timer countdown
    LaunchedEffect(isTimerRunning, timerGeneration) {
        if (!isTimerRunning) return@LaunchedEffect
        while (timerSeconds > 0) {
            delay(1000)
            timerSeconds--
        }
        // Timer reached 0
        isTimerRunning = false
        if (isOnBreak) {
            // Break finished - reset to initial focus duration
            isOnBreak = false
            timerSeconds = initialTimerSeconds
        } else {
            // Focus session finished - save to statistics
            statisticsRepository.saveSession(initialTimerSeconds / 60, initialTimerSeconds)
            // Always switch to break mode
            isOnBreak = true
            timerSeconds = breakDurationMinutes * 60
            if (autoBreakEnabled) {
                // Auto-start break countdown
                isTimerRunning = true
                timerGeneration++ // Force LaunchedEffect to restart
            }
            // If not autoBreakEnabled, timer stays stopped - user must press play
        }
    }
    
    // Immersive Mode Logic
    var isImmersiveMode by remember { mutableStateOf(false) }
    
    // Reset immersive mode when timer stops
    LaunchedEffect(isTimerRunning) {
        if (!isTimerRunning) {
            isImmersiveMode = false
        }
    }
    
    // Reset immersive mode when scrolling
    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            isImmersiveMode = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
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
        
        // Screen content based on pager state
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> StatisticsScreen(onNavigateToSettings, textColor)
                1 -> ClockScreen(currentTime, onNavigateToSettings, clockFontFamily, textColor, clockFont)
                2 -> TimerScreen(
                    isRunning = isTimerRunning,
                    seconds = timerSeconds,
                    isOnBreak = isOnBreak,
                    onStartStop = { 
                        if (isTimerRunning) {
                            // Pausing timer
                            isTimerRunning = false
                            if (!isOnBreak) {
                                // Only save to stats if it was a focus session
                                val elapsedSeconds = initialTimerSeconds - timerSeconds
                                if (elapsedSeconds > 0) {
                                    statisticsRepository.saveSession(elapsedSeconds / 60, elapsedSeconds)
                                }
                            }
                        } else {
                            // Starting timer - capture initial duration only for focus sessions
                            if (!isOnBreak) {
                                initialTimerSeconds = timerSeconds
                            }
                            isTimerRunning = true
                        }
                    },
                    onTimerClick = { 
                        if (!isTimerRunning && !isOnBreak) {
                            showDurationPicker = true 
                        }
                    },
                    onFinish = {
                        if (isOnBreak) {
                            // Skip/end break - reset to focus duration
                            isOnBreak = false
                            isTimerRunning = false
                            timerSeconds = initialTimerSeconds
                        } else {
                            // Finish/end the focus session - save elapsed time
                            val elapsedSeconds = initialTimerSeconds - timerSeconds
                            isTimerRunning = false
                            if (elapsedSeconds > 0) {
                                statisticsRepository.saveSession(elapsedSeconds / 60, elapsedSeconds)
                            }
                            // Reset to initial duration
                            timerSeconds = initialTimerSeconds
                        }
                    },
                    onNavigateToSettings = onNavigateToSettings,
                    clockFontFamily = clockFontFamily,
                    textColor = textColor,
                    isImmersiveMode = isImmersiveMode,
                    onToggleImmersiveMode = { 
                        if (isTimerRunning) {
                            isImmersiveMode = !isImmersiveMode
                        }
                    }
                )
            }
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
                AnimatedVisibility(
                    visible = !isImmersiveMode,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    NavigationDot(
                        isActive = index == pagerState.currentPage,
                        onClick = { 
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
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
    textColor: Color,
    clockFontKey: String = "menil"
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Settings icon with absolute positioning (7% from top, 7% from right)
        SettingsIconButton(onNavigateToSettings, textColor)
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Center clock display - perfectly centered
            val timeParts = currentTime.split("\n")
            val time = timeParts[0]
            val period = if (timeParts.size > 1) {
                val rawPeriod = timeParts[1]
                if (clockFontKey == "break") {
                    rawPeriod
                        .replace("ö", "o").replace("Ö", "O")
                        .replace("ş", "s").replace("Ş", "S")
                        .replace("ç", "c").replace("Ç", "C")
                        .replace("ğ", "g").replace("Ğ", "G")
                        .replace("ı", "i").replace("İ", "I")
                        .replace("ü", "u").replace("Ü", "U")
                } else rawPeriod
            } else ""
            
            // Reference text for scale calculation (measures at 240sp to see if it fits)
            val referenceText = if (period.isNotEmpty()) "$time $period" else time
            val baseStyle = TextStyle(
                fontFamily = clockFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 2.sp
            )
            
            ProportionalScaleBox(
                referenceText = referenceText,
                referenceStyle = baseStyle,
                referenceFontSize = 240.sp
            ) { scale ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = time,
                        style = TextStyle(
                            fontFamily = clockFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 240.sp * scale,
                            lineHeight = 240.sp * scale,
                            letterSpacing = 2.sp,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                    )
                    
                    if (period.isNotEmpty()) {
                        Spacer(modifier = Modifier.width((4 * scale).dp))
                        Text(
                            text = period,
                            style = TextStyle(
                                fontFamily = clockFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 75.sp * scale,
                                lineHeight = 75.sp * scale,
                                color = textColor,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerScreen(
    isRunning: Boolean,
    seconds: Int,
    isOnBreak: Boolean,
    onStartStop: () -> Unit,
    onTimerClick: () -> Unit,
    onFinish: () -> Unit,
    onNavigateToSettings: () -> Unit,
    clockFontFamily: FontFamily,
    textColor: Color,
    isImmersiveMode: Boolean,
    onToggleImmersiveMode: () -> Unit
) {
    val context = LocalContext.current
    
    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(onTap = { onToggleImmersiveMode() })
        }
    ) {
        // Settings icon with absolute positioning (7% from top, 7% from right)
        androidx.compose.animation.AnimatedVisibility(
            visible = !isImmersiveMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            SettingsIconButton(onNavigateToSettings, textColor)
        }
        
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
                // Break label + Timer display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Show break label when on break
                    if (isOnBreak) {
                        AnimatedVisibility(
                            visible = !isImmersiveMode,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.on_break_label),
                                    style = TextStyle(
                                        fontFamily = GeistFontFamily,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = textColor.copy(alpha = 0.4f),
                                        letterSpacing = 2.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    
                    // Timer color: lighter during break, normal otherwise
                    val timerColor = if (isOnBreak) textColor.copy(alpha = 0.4f) else textColor
                    
                    // Timer display with hour label - proportionally scaled
                    val totalMinutes = seconds / 60
                    val hours = totalMinutes / 60
                    val remainingMinutes = totalMinutes % 60
                    val remainingSeconds = seconds % 60
                    
                    // Build reference text for scale calculation
                    val hourPrefix = if (totalMinutes >= 60) {
                        if (hours == 1) {
                            "1 " + context.getString(R.string.hour_singular) + " "
                        } else {
                            "$hours " + context.getString(R.string.hours_plural) + " "
                        }
                    } else ""
                    
                    val timeStr = if (totalMinutes >= 60) {
                        String.format("%02d:%02d", remainingMinutes, remainingSeconds)
                    } else {
                        String.format("%02d:%02d", totalMinutes, remainingSeconds)
                    }
                    
                    val baseStyle = TextStyle(
                        fontFamily = clockFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                    
                    ProportionalScaleBox(
                        referenceText = hourPrefix + timeStr,
                        referenceStyle = baseStyle,
                        referenceFontSize = 240.sp,
                        modifier = Modifier.padding(horizontal = 56.dp)
                    ) { scale ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.clickable {
                                if (!isRunning && !isOnBreak) {
                                    onTimerClick()
                                } else {
                                    onToggleImmersiveMode()
                                }
                            }
                        ) {
                            // Show hour label if >= 1 hour
                            if (totalMinutes >= 60) {
                                Text(
                                    text = hourPrefix.trimEnd(),
                                    style = TextStyle(
                                        fontFamily = clockFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 70.sp * scale,
                                        color = timerColor,
                                        textAlign = TextAlign.End
                                    ),
                                    modifier = Modifier.padding(end = (8 * scale).dp)
                                )
                            }
                            
                            Text(
                                text = timeStr,
                                style = TextStyle(
                                    fontFamily = clockFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 240.sp * scale,
                                    color = timerColor,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
            } // Close Column wrapping break label + timer
            
            // Start/Stop button on the far left
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isImmersiveMode,
                    enter = fadeIn(),
                    exit = fadeOut()
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
            
            // Finish button on the far right (always visible)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isImmersiveMode,
                    enter = fadeIn(),
                    exit = fadeOut()
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

private fun getCurrentTimeString(amString: String, pmString: String, is24HourFormat: Boolean = false): String {
    val calendar = Calendar.getInstance()
    
    if (is24HourFormat) {
        // 24-hour format: HH:mm (no period)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return String.format("%02d:%02d", hour, minute)
    } else {
        // 12-hour format: hh:mm
        //                 period
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        // Use localized AM/PM
        val period = if (hour < 12) amString else pmString
        
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        return String.format("%02d:%02d\n%s", displayHour, minute, period)
    }
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
