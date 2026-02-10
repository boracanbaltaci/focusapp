package com.focusapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContext
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
    
    // Auto-refresh data every ~5 seconds so stats are always up-to-date
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

    // Animate bar appearance
    var animationTriggered by remember { mutableStateOf(false) }
    val animationProgress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "barAnimation"
    )
    LaunchedEffect(viewMode) {
        animationTriggered = false
        animationTriggered = true
    }

    // Theme-aware accent color
    val isDark = textColor != Color.Black
    val accentColor = Color(0xFF4CAF50)
    val accentGradientStart = Color(0xFF66BB6A)
    val accentGradientEnd = Color(0xFF388E3C)
    val subtleTextColor = textColor.copy(alpha = 0.5f)
    val chipBgUnselected = if (isDark) Color(0xFF2A2E24) else Color(0xFFF0F0F0)
    val dividerColor = textColor.copy(alpha = 0.08f)

    Box(modifier = Modifier.fillMaxSize()) {
        // Settings icon
        SettingsIconButton(onClick = onNavigateToSettings, iconColor = textColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Title
            Text(
                text = stringResource(R.string.statistics_title),
                style = TextStyle(
                    fontFamily = GeistFontFamily,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // View mode selector (pill-style)
            Row(
                modifier = Modifier
                    .background(chipBgUnselected, RoundedCornerShape(24.dp))
                    .padding(4.dp),
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

            Spacer(modifier = Modifier.height(28.dp))

            // Total time display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (hours > 0) "$hours" else "$minutes",
                    style = TextStyle(
                        fontFamily = GeistFontFamily,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (hours > 0) {
                        stringResource(R.string.stat_hours)
                    } else {
                        stringResource(R.string.stat_minutes)
                    },
                    style = TextStyle(
                        fontFamily = GeistFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = subtleTextColor
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (hours > 0 && minutes > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$minutes",
                        style = TextStyle(
                            fontFamily = GeistFontFamily,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.stat_minutes),
                        style = TextStyle(
                            fontFamily = GeistFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = subtleTextColor
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            Text(
                text = stringResource(R.string.stat_total_focus),
                style = TextStyle(
                    fontFamily = GeistFontFamily,
                    fontSize = 14.sp,
                    color = subtleTextColor
                )
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Thin divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(dividerColor)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bar chart with labels
            val scrollState = rememberScrollState()
            val needsScroll = viewMode == ViewMode.MONTH

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                Column(
                    modifier = if (needsScroll) {
                        Modifier.horizontalScroll(scrollState)
                    } else {
                        Modifier
                    }
                ) {
                    StyledBarChart(
                        data = data,
                        viewMode = viewMode,
                        animationProgress = animationProgress,
                        gradientStart = accentGradientStart,
                        gradientEnd = accentGradientEnd,
                        textColor = textColor,
                        subtleTextColor = subtleTextColor,
                        modifier = if (needsScroll) {
                            Modifier
                                .width((data.size * 28).dp)
                                .height(200.dp)
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    StyledChartLabels(
                        data = data,
                        viewMode = viewMode,
                        textColor = subtleTextColor,
                        modifier = if (needsScroll) {
                            Modifier.width((data.size * 28).dp)
                        } else {
                            Modifier.fillMaxWidth()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
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
            .padding(horizontal = 20.dp, vertical = 10.dp),
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
private fun StyledBarChart(
    data: Map<Int, Int>,
    viewMode: ViewMode,
    animationProgress: Float,
    gradientStart: Color,
    gradientEnd: Color,
    textColor: Color,
    subtleTextColor: Color,
    modifier: Modifier = Modifier
) {
    val maxValue = data.values.maxOrNull()?.takeIf { it > 0 } ?: 1
    val sortedEntries = data.entries.sortedBy { it.key }

    Canvas(modifier = modifier) {
        val chartWidth = size.width
        val chartHeight = size.height
        val barCount = sortedEntries.size
        if (barCount == 0) return@Canvas

        val totalBarAreaWidth = chartWidth / barCount
        val barWidth = totalBarAreaWidth * 0.55f
        val cornerRadiusPx = barWidth / 2f

        // Draw subtle horizontal grid lines
        val gridLineCount = 4
        for (i in 1..gridLineCount) {
            val y = chartHeight - (chartHeight * i / (gridLineCount + 1))
            drawLine(
                color = subtleTextColor.copy(alpha = 0.15f),
                start = Offset(0f, y),
                end = Offset(chartWidth, y),
                strokeWidth = 0.5f
            )
        }

        sortedEntries.forEachIndexed { index, entry ->
            val rawBarHeight = (entry.value.toFloat() / maxValue) * (chartHeight - cornerRadiusPx)
            val barHeight = rawBarHeight * animationProgress

            if (barHeight > 0f) {
                val centerX = index * totalBarAreaWidth + totalBarAreaWidth / 2f
                val x = centerX - barWidth / 2f
                val y = chartHeight - barHeight

                // Gradient brush from bottom to top
                val brush = Brush.verticalGradient(
                    colors = listOf(gradientEnd, gradientStart),
                    startY = chartHeight,
                    endY = y
                )

                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                )
            }
        }
    }
}

@Composable
private fun StyledChartLabels(
    data: Map<Int, Int>,
    viewMode: ViewMode,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sortedKeys = data.keys.sorted()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        sortedKeys.forEach { key ->
            val label = when (viewMode) {
                ViewMode.WEEK -> {
                    when (key) {
                        1 -> context.getString(R.string.day_mon)
                        2 -> context.getString(R.string.day_tue)
                        3 -> context.getString(R.string.day_wed)
                        4 -> context.getString(R.string.day_thu)
                        5 -> context.getString(R.string.day_fri)
                        6 -> context.getString(R.string.day_sat)
                        7 -> context.getString(R.string.day_sun)
                        else -> ""
                    }
                }
                ViewMode.MONTH -> {
                    // Show every 5th day label + first and last
                    if (key == 1 || key % 5 == 0 || key == sortedKeys.last()) {
                        key.toString()
                    } else ""
                }
                ViewMode.YEAR -> {
                    when (key) {
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

            Text(
                text = label,
                style = TextStyle(
                    fontFamily = GeistFontFamily,
                    fontSize = 10.sp,
                    color = textColor,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
