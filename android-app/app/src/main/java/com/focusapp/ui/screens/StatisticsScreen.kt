package com.focusapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusapp.R
import com.focusapp.data.StatisticsRepository
import com.focusapp.ui.theme.GeistFontFamily
import kotlinx.coroutines.delay

enum class ViewMode {
    WEEK, MONTH, YEAR
}

@Composable
fun StatisticsScreen(
    onNavigateToSettings: () -> Unit,
    textColor: Color
) {
    val context = LocalContext.current
    val statisticsRepository = remember { StatisticsRepository(context) }
    var viewMode by remember { mutableStateOf(ViewMode.WEEK) }

    // Auto-refresh data every ~5 seconds
    var refreshTick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            refreshTick++
        }
    }

    val data = remember(viewMode, refreshTick) {
        when (viewMode) {
            ViewMode.WEEK -> statisticsRepository.getWeeklyData()
            ViewMode.MONTH -> statisticsRepository.getMonthlyData()
            ViewMode.YEAR -> statisticsRepository.getYearlyData()
        }
    }

    val totalMinutes = statisticsRepository.getTotalMinutes(data)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    // Theme-aware colors
    val isDark = textColor != Color.Black
    val accentColor = Color(0xFF4CAF50)
    val gradientStart = Color(0xFF66BB6A)
    val gradientEnd = Color(0xFF388E3C)
    val subtleTextColor = textColor.copy(alpha = 0.5f)
    val chipBg = if (isDark) Color(0xFF2A2E24) else Color(0xFFF0F0F0)
    val dividerColor = textColor.copy(alpha = 0.08f)
    val gridLineColor = textColor.copy(alpha = 0.06f)

    // Swipe gesture to switch view modes
    val viewModes = ViewMode.entries
    Box(modifier = Modifier.fillMaxSize()) {
        SettingsIconButton(onClick = onNavigateToSettings, iconColor = textColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Title
            Text(
                text = stringResource(R.string.statistics_title),
                style = TextStyle(
                    fontFamily = GeistFontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Pill-style view mode selector (swipeable)
            Row(
                modifier = Modifier
                    .background(chipBg, RoundedCornerShape(20.dp))
                    .padding(3.dp)
                    .pointerInput(viewMode) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { totalDrag = 0f },
                            onDragEnd = {
                                val currentIndex = viewModes.indexOf(viewMode)
                                if (totalDrag < -80f && currentIndex < viewModes.lastIndex) {
                                    viewMode = viewModes[currentIndex + 1]
                                } else if (totalDrag > 80f && currentIndex > 0) {
                                    viewMode = viewModes[currentIndex - 1]
                                }
                            },
                            onDragCancel = { totalDrag = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount
                            }
                        )
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ViewModeChip(
                    text = stringResource(R.string.stat_week),
                    selected = viewMode == ViewMode.WEEK,
                    onClick = { viewMode = ViewMode.WEEK },
                    accentColor = accentColor
                )
                ViewModeChip(
                    text = stringResource(R.string.stat_month),
                    selected = viewMode == ViewMode.MONTH,
                    onClick = { viewMode = ViewMode.MONTH },
                    accentColor = accentColor
                )
                ViewModeChip(
                    text = stringResource(R.string.stat_year),
                    selected = viewMode == ViewMode.YEAR,
                    onClick = { viewMode = ViewMode.YEAR },
                    accentColor = accentColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total time display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (hours > 0) "$hours" else "$minutes",
                    style = TextStyle(
                        fontFamily = GeistFontFamily,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (hours > 0) stringResource(R.string.stat_hours)
                           else stringResource(R.string.stat_minutes),
                    style = TextStyle(
                        fontFamily = GeistFontFamily,
                        fontSize = 15.sp,
                        color = subtleTextColor
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                if (hours > 0 && minutes > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$minutes",
                        style = TextStyle(
                            fontFamily = GeistFontFamily,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.stat_minutes),
                        style = TextStyle(
                            fontFamily = GeistFontFamily,
                            fontSize = 15.sp,
                            color = subtleTextColor
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            Text(
                text = stringResource(R.string.stat_total_focus),
                style = TextStyle(
                    fontFamily = GeistFontFamily,
                    fontSize = 12.sp,
                    color = subtleTextColor
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(dividerColor)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- Bar Chart with dynamic grid ---
            val sortedEntries = data.entries.sortedBy { it.key }
            val scrollState = rememberScrollState()
            val needsScroll = viewMode == ViewMode.MONTH
            val hourAbbrev = stringResource(R.string.stat_hour_grid)

            // Dynamic grid scale per view mode
            val gridSteps = when (viewMode) {
                ViewMode.WEEK -> listOf(1, 2, 3, 4, 5)
                ViewMode.MONTH -> listOf(5, 10, 15, 20, 25)
                ViewMode.YEAR -> listOf(5, 10, 15, 20, 25)
            }
            val gridMaxMinutes = gridSteps.last() * 60 // max scale in minutes

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp)
            ) {
                // Layer 1: Grid lines with Y-axis labels
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    for (h in gridSteps.reversed()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "$h$hourAbbrev",
                                style = TextStyle(
                                    fontFamily = GeistFontFamily,
                                    fontSize = 8.sp,
                                    color = subtleTextColor,
                                    textAlign = TextAlign.End
                                ),
                                modifier = Modifier.width(24.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(0.5.dp)
                                    .background(gridLineColor)
                            )
                        }
                    }
                }

                // Layer 2: Bars + day labels (overlaid on grid)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 28.dp)
                        .then(
                            if (needsScroll) Modifier.horizontalScroll(scrollState)
                            else Modifier
                        ),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    sortedEntries.forEach { entry ->
                        val fraction = (entry.value.toFloat() / gridMaxMinutes.toFloat()).coerceIn(0f, 1f)
                        val label = getLabelForKey(entry.key, viewMode)

                        BarItem(
                            fraction = fraction,
                            label = label,
                            totalMinutes = entry.value,
                            gradientStart = gradientStart,
                            gradientEnd = gradientEnd,
                            subtleTextColor = subtleTextColor,
                            barCount = sortedEntries.size,
                            needsScroll = needsScroll
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
private fun RowScope.BarItem(
    fraction: Float,
    label: String,
    totalMinutes: Int,
    gradientStart: Color,
    gradientEnd: Color,
    subtleTextColor: Color,
    barCount: Int,
    needsScroll: Boolean
) {
    // Animate bar height
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 500),
        label = "barHeight"
    )

    val barWidth = if (needsScroll) 10.dp else {
        when {
            barCount <= 7 -> 16.dp
            barCount <= 12 -> 12.dp
            else -> 8.dp
        }
    }

    // Tooltip state
    var showTooltip by remember { mutableStateOf(false) }
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    val hourLabel = stringResource(R.string.stat_hours)
    val minuteLabel = stringResource(R.string.stat_minutes)
    val tooltipText = when {
        h > 0 && m > 0 -> "$h$hourLabel $m$minuteLabel"
        h > 0 -> "$h$hourLabel"
        else -> "$m$minuteLabel"
    }

    Column(
        modifier = if (needsScroll) {
            Modifier.width(24.dp)
        } else {
            Modifier.weight(1f)
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Bar — fills available height
        Box(
            modifier = Modifier
                .weight(1f)
                .width(barWidth),
            contentAlignment = Alignment.BottomCenter
        ) {
            val barHeightFraction = animatedFraction.coerceIn(0f, 1f)
            if (barHeightFraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(barHeightFraction)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(gradientStart, gradientEnd)
                            )
                        )
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                showTooltip = true
                                waitForUpOrCancellation()
                                showTooltip = false
                            }
                        }
                )
            }

            // Tooltip popup above the bar
            if (showTooltip && totalMinutes > 0) {
                Popup(
                    alignment = Alignment.TopCenter,
                    offset = IntOffset(0, -48)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = tooltipText,
                            style = TextStyle(
                                fontFamily = GeistFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Label
        Text(
            text = label,
            style = TextStyle(
                fontFamily = GeistFontFamily,
                fontSize = 9.sp,
                color = subtleTextColor,
                textAlign = TextAlign.Center
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun ViewModeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(
                color = if (selected) accentColor else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = GeistFontFamily,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) Color.White else Color.Gray
            )
        )
    }
}

@Composable
private fun getLabelForKey(key: Int, viewMode: ViewMode): String {
    val context = LocalContext.current
    return when (viewMode) {
        ViewMode.WEEK -> when (key) {
            1 -> context.getString(R.string.day_mon)
            2 -> context.getString(R.string.day_tue)
            3 -> context.getString(R.string.day_wed)
            4 -> context.getString(R.string.day_thu)
            5 -> context.getString(R.string.day_fri)
            6 -> context.getString(R.string.day_sat)
            7 -> context.getString(R.string.day_sun)
            else -> ""
        }
        ViewMode.MONTH -> {
            if (key == 1 || key % 5 == 0) key.toString() else ""
        }
        ViewMode.YEAR -> when (key) {
            1 -> context.getString(R.string.month_jan)
            2 -> context.getString(R.string.month_feb)
            3 -> context.getString(R.string.month_mar)
            4 -> context.getString(R.string.month_apr)
            5 -> context.getString(R.string.month_may)
            6 -> context.getString(R.string.month_jun)
            7 -> context.getString(R.string.month_jul)
            8 -> context.getString(R.string.month_aug)
            9 -> context.getString(R.string.month_sep)
            10 -> context.getString(R.string.month_oct)
            11 -> context.getString(R.string.month_nov)
            12 -> context.getString(R.string.month_dec)
            else -> ""
        }
    }
}
