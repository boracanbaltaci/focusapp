package com.focusapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.clockera.R
import com.focusapp.data.StatisticsRepository
import com.focusapp.ui.components.LocalScreenScale
import com.focusapp.ui.components.ProportionalScaleBox
import com.focusapp.ui.components.scaled
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
    val colorPairIndex by settingsViewModel.colorPairIndex.collectAsState()
    
    // Default theme colors (used for statistics screen always)
    val defaultBackgroundColor = if (theme == "dark") Color(0xFF181C14) else Color(0xFFFBFBFB)
    val defaultTextColor = if (theme == "dark") Color(0xFFECDFCC) else Color.Black
    
    // Color pairs: each is (lightColor, darkColor)
    val colorPairs = listOf(
        Pair(Color(0xFFFDFDC9), Color(0xFFC69FD5)), // Lemon + Wisteria
        Pair(Color(0xFFCBD9FF), Color(0xFF3F6048)), // Periwinkle + Hunter Green
        Pair(Color(0xFFBDDBF7), Color(0xFF6071B6)), // Uranian Blue + Glaucous
        Pair(Color(0xFFBAD797), Color(0xFF670626)), // Matcha + Cherry
        Pair(Color(0xFFC2D8C4), Color(0xFF222222)), // Matcha Mist + Dusty Coal
        Pair(Color(0xFFFAF0CA), Color(0xFF0D3B66))  // Lemon Chiffon + Yale Blue
    )
    
    // Effective colors for Clock/Timer screens
    val effectiveBackgroundColor: Color
    val effectiveTextColor: Color
    if (colorPairIndex in 1..colorPairs.size) {
        val pair = colorPairs[colorPairIndex - 1]
        if (theme == "dark") {
            effectiveBackgroundColor = pair.second // dark color as bg
            effectiveTextColor = pair.first         // light color as text
        } else {
            effectiveBackgroundColor = pair.first   // light color as bg
            effectiveTextColor = pair.second        // dark color as text
        }
    } else {
        effectiveBackgroundColor = defaultBackgroundColor
        effectiveTextColor = defaultTextColor
    }
    
    val backgroundColor = effectiveBackgroundColor
    val textColor = effectiveTextColor
    val arcColor = effectiveTextColor.copy(alpha = 0.15f)
    
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
    var selectedCategory by remember { mutableStateOf<FocusCategory?>(null) }
    
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
            statisticsRepository.saveSession(initialTimerSeconds / 60, initialTimerSeconds, selectedCategory?.name)
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
    var showColorPicker by remember { mutableStateOf(false) }
    
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
                0 -> StatisticsScreen(onNavigateToSettings, defaultTextColor)
                1 -> ClockScreen(
                    currentTime = currentTime,
                    onNavigateToSettings = onNavigateToSettings,
                    onOpenColorPicker = { showColorPicker = true },
                    clockFontFamily = clockFontFamily,
                    textColor = textColor,
                    clockFontKey = clockFont,
                    isImmersiveMode = isImmersiveMode,
                    onToggleImmersiveMode = { isImmersiveMode = !isImmersiveMode }
                )
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
                                    statisticsRepository.saveSession(elapsedSeconds / 60, elapsedSeconds, selectedCategory?.name)
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
                                statisticsRepository.saveSession(elapsedSeconds / 60, elapsedSeconds, selectedCategory?.name)
                            }
                            // Reset to initial duration
                            timerSeconds = initialTimerSeconds
                        }
                    },
                    onNavigateToSettings = onNavigateToSettings,
                    onOpenColorPicker = { showColorPicker = true },
                    clockFontFamily = clockFontFamily,
                    textColor = textColor,
                    isImmersiveMode = isImmersiveMode,
                    onToggleImmersiveMode = { 
                        isImmersiveMode = !isImmersiveMode
                    },
                    isDark = theme == "dark",
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
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
                },
                isDark = theme == "dark"
            )
        }
        
        // Color picker modal
        if (showColorPicker) {
            ColorPickerModal(
                colorPairs = colorPairs,
                selectedIndex = colorPairIndex,
                isDark = theme == "dark",
                onSelect = { index ->
                    settingsViewModel.setColorPairIndex(index)
                },
                onDismiss = { showColorPicker = false }
            )
        }
        
        // Bottom navigation dots
        val navScale = LocalScreenScale.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp.scaled(navScale, min = 16.dp)),
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
                    Spacer(modifier = Modifier.width(16.dp.scaled(navScale, min = 8.dp)))
                }
            }
        }
    }
}

@Composable
private fun ClockScreen(
    currentTime: String,
    onNavigateToSettings: () -> Unit,
    onOpenColorPicker: () -> Unit,
    clockFontFamily: FontFamily,
    textColor: Color,
    clockFontKey: String = "menil",
    isImmersiveMode: Boolean = false,
    onToggleImmersiveMode: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                onToggleImmersiveMode()
            }
    ) {
        // Settings icon with absolute positioning (7% from top, 7% from right)
        AnimatedVisibility(
            visible = !isImmersiveMode,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SettingsIconButton(onNavigateToSettings, textColor)
        }
        
        // Color picker icon (top-left)
        AnimatedVisibility(
            visible = !isImmersiveMode,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ColorPickerIconButton(onClick = onOpenColorPicker, iconColor = textColor)
        }
        
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
    onOpenColorPicker: () -> Unit,
    clockFontFamily: FontFamily,
    textColor: Color,
    isImmersiveMode: Boolean,
    onToggleImmersiveMode: () -> Unit,
    isDark: Boolean = false,
    selectedCategory: FocusCategory? = null,
    onCategorySelected: (FocusCategory?) -> Unit = {}
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
        
        // Color picker icon (top-left)
        androidx.compose.animation.AnimatedVisibility(
            visible = !isImmersiveMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            ColorPickerIconButton(onClick = onOpenColorPicker, iconColor = textColor)
        }
        
        // Main content
        val timerScale = LocalScreenScale.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp.scaled(timerScale, min = 16.dp)),
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
                                        fontSize = 24.sp.scaled(timerScale, min = 14.sp),
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
                            .padding(start = 24.dp.scaled(timerScale, min = 8.dp))
                            .size(56.dp.scaled(timerScale, min = 36.dp)),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        // Play or Pause icon using Canvas
                        Canvas(modifier = Modifier.size(24.dp.scaled(timerScale, min = 16.dp))) {
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
                            .padding(end = 24.dp.scaled(timerScale, min = 8.dp))
                            .size(56.dp.scaled(timerScale, min = 36.dp)),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        // Stop/Finish icon (square) using Canvas
                        Canvas(modifier = Modifier.size(20.dp.scaled(timerScale, min = 14.dp))) {
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

        // Pin category selector at bottom-left, aligned with color picker icon
        // Uses same 7% offset as ColorPickerIconButton but mirrored to bottom
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val pinIconScale = LocalScreenScale.current
            val pinIconSize = 32.dp.scaled(pinIconScale, min = 24.dp)
            val pinXOffset = maxWidth * 0.07f - (pinIconSize / 2)
            val pinYOffset = maxHeight * 0.88f - pinIconSize

            androidx.compose.animation.AnimatedVisibility(
                visible = !isImmersiveMode,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.offset(x = pinXOffset, y = pinYOffset)
            ) {
                PinCategorySelector(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        onCategorySelected(category)
                    },
                    textColor = textColor,
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
fun SettingsIconButton(onClick: () -> Unit, iconColor: Color) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val iconScale = LocalScreenScale.current
        // Position: 7% from top, 7% from right (93% from left)
        val iconSize = 32.dp.scaled(iconScale, min = 24.dp)
        val xOffset = maxWidth * 0.93f - (iconSize / 2)  // 7% from right
        val yOffset = maxHeight * 0.07f          // 7% from top
        
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(iconSize)
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
    onDurationSelected: (Int) -> Unit,
    isDark: Boolean = false
) {
    val hourShort = stringResource(R.string.hour_short)
    val minShort = stringResource(R.string.minute_short)
    
    // Theme colors
    val dialogBg = if (isDark) Color(0xFF1E2218) else Color(0xFFFBFBFB)
    val textPrimary = if (isDark) Color(0xFFECDFCC) else Color(0xFF1A1A1A)
    val textSecondary = if (isDark) Color(0xFFECDFCC).copy(alpha = 0.4f) else Color(0xFF999999)
    val dividerColor = if (isDark) Color(0xFFECDFCC).copy(alpha = 0.08f) else Color(0xFFE0E0E0)
    
    val durationOptions = listOf(
        Pair("5 $minShort", 5 * 60),
        Pair("10 $minShort", 10 * 60),
        Pair("15 $minShort", 15 * 60),
        Pair("20 $minShort", 20 * 60),
        Pair("30 $minShort", 30 * 60),
        Pair("45 $minShort", 45 * 60),
        Pair("1 $hourShort", 60 * 60),
        Pair("1 $hourShort 10 $minShort", 70 * 60),
        Pair("1 $hourShort 15 $minShort", 75 * 60),
        Pair("1 $hourShort 20 $minShort", 80 * 60),
        Pair("1 $hourShort 30 $minShort", 90 * 60),
        Pair("1 $hourShort 45 $minShort", 105 * 60),
        Pair("2 $hourShort", 120 * 60)
    )
    
    val listState = rememberLazyListState()
    
    Dialog(onDismissRequest = onDismiss) {
        val dialogScale = LocalScreenScale.current
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp.scaled(dialogScale, min = 16.dp)),
            color = dialogBg,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = stringResource(R.string.select_duration),
                    style = TextStyle(
                        fontFamily = GeistFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Subtle divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(0.5.dp)
                        .background(dividerColor)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Scrollable duration list - tap to select and apply
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp.scaled(dialogScale, min = 200.dp))
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(durationOptions.size) { index ->
                        val (label, seconds) = durationOptions[index]
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    onDurationSelected(seconds)
                                }
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = label,
                                style = TextStyle(
                                    fontFamily = GeistFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = textSecondary
                                )
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ColorPickerIconButton(onClick: () -> Unit, iconColor: Color) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val iconScale = LocalScreenScale.current
        // Position: 7% from top, 7% from left (mirrors settings icon)
        val iconSize = 32.dp.scaled(iconScale, min = 24.dp)
        val xOffset = maxWidth * 0.07f - (iconSize / 2)
        val yOffset = maxHeight * 0.07f
        
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(iconSize)
                .offset(x = xOffset, y = yOffset)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val r = size.width * 0.38f
                val strokeW = 2.dp.toPx()
                
                // Draw eyedropper icon
                // Main body (angled rectangle)
                val bodyPath = Path().apply {
                    moveTo(centerX + r * 0.1f, centerY - r * 0.9f)
                    lineTo(centerX + r * 0.5f, centerY - r * 0.5f)
                    lineTo(centerX - r * 0.4f, centerY + r * 0.6f)
                    lineTo(centerX - r * 0.8f, centerY + r * 0.2f)
                    close()
                }
                drawPath(
                    path = bodyPath,
                    color = iconColor,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
                
                // Tip (triangle)
                val tipPath = Path().apply {
                    moveTo(centerX - r * 0.4f, centerY + r * 0.6f)
                    lineTo(centerX - r * 0.65f, centerY + r * 0.85f)
                    lineTo(centerX - r * 0.8f, centerY + r * 0.2f)
                }
                drawPath(
                    path = tipPath,
                    color = iconColor,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
                
                // Top bulb (circle)
                drawCircle(
                    color = iconColor,
                    radius = r * 0.22f,
                    center = Offset(centerX + r * 0.3f, centerY - r * 0.7f),
                    style = Stroke(width = strokeW)
                )
            }
        }
    }
}

@Composable
private fun ColorPickerModal(
    colorPairs: List<Pair<Color, Color>>,
    selectedIndex: Int,
    isDark: Boolean,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val dialogBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF8F8F8)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f)
    val checkColor = if (isDark) Color.White else Color.Black
    
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(dialogBg)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Default (no custom color) option
                val isDefaultSelected = selectedIndex == 0
                
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDefaultSelected) checkColor.copy(alpha = 0.1f)
                            else Color.Transparent
                        )
                        .clickable { onSelect(0); onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    // Reset icon (circle with slash)
                    Canvas(modifier = Modifier.size(28.dp)) {
                        val s = size.width
                        val sw = 2.dp.toPx()
                        drawCircle(
                            color = checkColor.copy(alpha = 0.5f),
                            radius = s * 0.4f,
                            style = Stroke(width = sw)
                        )
                        drawLine(
                            color = checkColor.copy(alpha = 0.5f),
                            start = Offset(s * 0.25f, s * 0.75f),
                            end = Offset(s * 0.75f, s * 0.25f),
                            strokeWidth = sw,
                            cap = StrokeCap.Round
                        )
                    }
                    if (isDefaultSelected) {
                        Canvas(modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.BottomEnd)
                        ) {
                            val path = Path().apply {
                                moveTo(size.width * 0.15f, size.height * 0.5f)
                                lineTo(size.width * 0.4f, size.height * 0.8f)
                                lineTo(size.width * 0.85f, size.height * 0.2f)
                            }
                            drawPath(
                                path = path,
                                color = checkColor,
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            )
                        }
                    }
                }
                
                Divider(
                    color = borderColor,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                // Color pair circles in grid (rows of 3)
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    colorPairs.chunked(3).forEachIndexed { rowIndex, rowPairs ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            rowPairs.forEachIndexed { colIndex, pair ->
                                val pairIndex = rowIndex * 3 + colIndex + 1
                                val isSelected = selectedIndex == pairIndex
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isSelected) checkColor.copy(alpha = 0.08f)
                                            else Color.Transparent
                                        )
                                        .clickable { onSelect(pairIndex); onDismiss() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Two overlapping circles for each pair
                                    val cpScale = LocalScreenScale.current
                                    val circleContainerSize = 48.dp.scaled(cpScale, min = 32.dp)
                                    val circleSize = 32.dp.scaled(cpScale, min = 22.dp)
                                    Box(modifier = Modifier.size(circleContainerSize)) {
                                        // Light color circle (back-left)
                                        Box(
                                            modifier = Modifier
                                                .size(circleSize)
                                                .align(Alignment.TopStart)
                                                .shadow(2.dp, CircleShape)
                                                .clip(CircleShape)
                                                .background(pair.first)
                                        )
                                        // Dark color circle (front-right)
                                        Box(
                                            modifier = Modifier
                                                .size(circleSize)
                                                .align(Alignment.BottomEnd)
                                                .shadow(2.dp, CircleShape)
                                                .clip(CircleShape)
                                                .background(pair.second)
                                        )
                                    }
                                    
                                    // Selection checkmark indicator
                                    if (isSelected) {
                                        Canvas(modifier = Modifier
                                            .size(14.dp)
                                            .align(Alignment.BottomCenter)
                                            .offset(y = 2.dp)
                                        ) {
                                            val path = Path().apply {
                                                moveTo(size.width * 0.15f, size.height * 0.5f)
                                                lineTo(size.width * 0.4f, size.height * 0.8f)
                                                lineTo(size.width * 0.85f, size.height * 0.2f)
                                            }
                                            drawPath(
                                                path = path,
                                                color = checkColor,
                                                style = Stroke(
                                                    width = 2.dp.toPx(),
                                                    cap = StrokeCap.Round
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                            // Fill remaining space if row has fewer than 3 items
                            repeat(3 - rowPairs.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
