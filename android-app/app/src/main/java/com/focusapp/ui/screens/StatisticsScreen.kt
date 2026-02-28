package com.focusapp.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import com.clockera.R
import com.focusapp.ui.components.LocalScreenScale
import com.focusapp.ui.components.scaled
import com.focusapp.data.StatisticsRepository
import com.focusapp.ui.theme.GeistFontFamily
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    var refreshTick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            refreshTick++
        }
    }

    val data = remember(viewMode, refreshTick, selectedCategoryFilter) {
        when (viewMode) {
            ViewMode.WEEK -> statisticsRepository.getWeeklyData(selectedCategoryFilter)
            ViewMode.MONTH -> statisticsRepository.getMonthlyData(selectedCategoryFilter)
            ViewMode.YEAR -> statisticsRepository.getYearlyData(selectedCategoryFilter)
        }
    }

    val categoryBreakdown = remember(refreshTick) {
        statisticsRepository.getWeeklyCategoryBreakdown()
    }

    val totalMinutes = statisticsRepository.getTotalMinutes(data)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    val isDark = textColor != Color.Black
    val accentColor = Color(0xFF4CAF50)
    val gradientStart = Color(0xFF66BB6A)
    val gradientEnd = Color(0xFF388E3C)
    val subtleTextColor = textColor.copy(alpha = 0.5f)
    val chipBg = if (isDark) Color(0xFF2A2E24) else Color(0xFFF0F0F0)
    val gridLineColor = textColor.copy(alpha = 0.06f)

    val viewModes = ViewMode.entries
    Box(modifier = Modifier.fillMaxSize()) {
        SettingsIconButton(onClick = onNavigateToSettings, iconColor = textColor)

        val statsScale = LocalScreenScale.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp.scaled(statsScale, min = 12.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp.scaled(statsScale, min = 20.dp)))

            // ── Hero: Total time ──
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (hours > 0) "$hours" else "$minutes",
                    style = TextStyle(
                        fontFamily = GeistFontFamily,
                        fontSize = 44.sp.scaled(statsScale, min = 28.sp),
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        letterSpacing = (-1).sp
                    )
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (hours > 0) stringResource(R.string.stat_hours)
                           else stringResource(R.string.stat_minutes),
                    style = TextStyle(
                        fontFamily = GeistFontFamily,
                        fontSize = 16.sp.scaled(statsScale, min = 12.sp),
                        fontWeight = FontWeight.Normal,
                        color = subtleTextColor
                    )
                )
                if (hours > 0 && minutes > 0) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "$minutes",
                        style = TextStyle(
                            fontFamily = GeistFontFamily,
                            fontSize = 44.sp.scaled(statsScale, min = 28.sp),
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            letterSpacing = (-1).sp
                        )
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(R.string.stat_minutes),
                        style = TextStyle(
                            fontFamily = GeistFontFamily,
                            fontSize = 16.sp.scaled(statsScale, min = 12.sp),
                            fontWeight = FontWeight.Normal,
                            color = subtleTextColor
                        )
                    )
                }
            }

            Text(
                text = stringResource(R.string.stat_total_focus),
                style = TextStyle(
                    fontFamily = GeistFontFamily,
                    fontSize = 12.sp,
                    color = subtleTextColor,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── View mode selector ──
            Row(
                modifier = Modifier
                    .background(chipBg, RoundedCornerShape(24.dp))
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

            Spacer(modifier = Modifier.height(10.dp))

            // ── Category filter ──
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val allSelected = selectedCategoryFilter == null
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (allSelected) accentColor.copy(alpha = 0.15f)
                            else Color.Transparent
                        )
                        .clickable { selectedCategoryFilter = null },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "T",
                        style = TextStyle(
                            fontFamily = GeistFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (allSelected) accentColor else textColor.copy(alpha = 0.35f)
                        )
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))

                FocusCategory.entries.forEach { category ->
                    val catName = category.name
                    val isSelected = selectedCategoryFilter == catName
                    val categoryColor = getCategoryColor(category)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) categoryColor.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .clickable {
                                selectedCategoryFilter = if (isSelected) null else catName
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(14.dp)) {
                            val iconColor = if (isSelected) categoryColor else textColor.copy(alpha = 0.35f)
                            when (category) {
                                FocusCategory.BOOK -> drawBookIcon(iconColor)
                                FocusCategory.WORK -> drawWorkIcon(iconColor)
                                FocusCategory.SPORT -> drawSportIcon(iconColor)
                                FocusCategory.COFFEE -> drawCoffeeIcon(iconColor)
                                FocusCategory.GAMING -> drawGamingIcon(iconColor)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Bar Chart ──
            val sortedEntries = data.entries.sortedBy { it.key }
            val scrollState = rememberScrollState()
            val needsScroll = viewMode == ViewMode.MONTH
            val hourAbbrev = stringResource(R.string.stat_hour_grid)
            val minAbbrev = stringResource(R.string.stat_minutes)

            val maxDataMinutes = data.values.maxOrNull() ?: 0
            val gridSteps: List<Int>
            val gridMaxMinutes: Int
            val gridIsMinutes: Boolean

            if (maxDataMinutes <= 0) {
                gridSteps = listOf(5, 10, 15, 20, 25); gridMaxMinutes = 25; gridIsMinutes = true
            } else if (maxDataMinutes <= 10) {
                gridSteps = listOf(2, 4, 6, 8, 10); gridMaxMinutes = 10; gridIsMinutes = true
            } else if (maxDataMinutes <= 30) {
                gridSteps = listOf(5, 10, 15, 20, 30); gridMaxMinutes = 30; gridIsMinutes = true
            } else if (maxDataMinutes <= 60) {
                gridSteps = listOf(10, 20, 30, 40, 60); gridMaxMinutes = 60; gridIsMinutes = true
            } else if (maxDataMinutes <= 180) {
                gridSteps = listOf(1, 2, 3); gridMaxMinutes = 3 * 60; gridIsMinutes = false
            } else if (maxDataMinutes <= 300) {
                gridSteps = listOf(1, 2, 3, 4, 5); gridMaxMinutes = 5 * 60; gridIsMinutes = false
            } else {
                val maxHours = (maxDataMinutes / 60) + 1
                val step = ((maxHours + 4) / 5).coerceAtLeast(1)
                val topHour = step * 5
                gridSteps = (1..5).map { it * step }; gridMaxMinutes = topHour * 60; gridIsMinutes = false
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 8.dp)
            ) {
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
                                text = if (gridIsMinutes) "$h$minAbbrev" else "$h$hourAbbrev",
                                style = TextStyle(
                                    fontFamily = GeistFontFamily,
                                    fontSize = 8.sp,
                                    color = textColor.copy(alpha = 0.3f),
                                    textAlign = TextAlign.End
                                ),
                                modifier = Modifier.width(if (gridIsMinutes) 30.dp else 24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(0.5.dp)
                                    .background(gridLineColor)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 30.dp)
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

            // ── Category Breakdown ──
            if (categoryBreakdown.isNotEmpty()) {
                val maxCategoryMinutes = categoryBreakdown.values.maxOrNull() ?: 1
                categoryBreakdown.entries.sortedByDescending { it.value }.forEach { (catName, catMinutes) ->
                    val category = try { FocusCategory.valueOf(catName) } catch (_: Exception) { return@forEach }
                    val categoryColor = getCategoryColor(category)
                    val catH = catMinutes / 60
                    val catM = catMinutes % 60
                    val catTimeText = when {
                        catH > 0 && catM > 0 -> "${catH}${stringResource(R.string.stat_hours)} ${catM}${stringResource(R.string.stat_minutes)}"
                        catH > 0 -> "${catH}${stringResource(R.string.stat_hours)}"
                        else -> "${catM}${stringResource(R.string.stat_minutes)}"
                    }
                    val barFraction = catMinutes.toFloat() / maxCategoryMinutes.toFloat()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas(modifier = Modifier.size(14.dp)) {
                            when (category) {
                                FocusCategory.BOOK -> drawBookIcon(categoryColor)
                                FocusCategory.WORK -> drawWorkIcon(categoryColor)
                                FocusCategory.SPORT -> drawSportIcon(categoryColor)
                                FocusCategory.COFFEE -> drawCoffeeIcon(categoryColor)
                                FocusCategory.GAMING -> drawGamingIcon(categoryColor)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(chipBg)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(barFraction)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(categoryColor.copy(alpha = 0.7f))
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = catTimeText,
                            style = TextStyle(
                                fontFamily = GeistFontFamily,
                                fontSize = 10.sp,
                                color = subtleTextColor
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp.scaled(statsScale, min = 24.dp)))
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
    val chipScale = LocalScreenScale.current
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(
                color = if (selected) accentColor else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp.scaled(chipScale, min = 8.dp), vertical = 8.dp.scaled(chipScale, min = 4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = GeistFontFamily,
                fontSize = 14.sp.scaled(chipScale, min = 10.sp),
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

// --- Pin Category Selector ---

enum class FocusCategory {
    BOOK, WORK, SPORT, COFFEE, GAMING
}

@Composable
fun PinCategorySelector(
    selectedCategory: FocusCategory?,
    onCategorySelected: (FocusCategory?) -> Unit,
    textColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val categories = FocusCategory.entries

    val accentColor = Color(0xFF4CAF50)
    val buttonBgColor = if (isDark) Color(0xFF2A2A2A) else Color(0xFFF5F5F5)
    val categoryBgColor = if (isDark) Color(0xFF2A2E24) else Color(0xFFF0F0F0)

    val buttonScale by animateFloatAsState(
        targetValue = if (isExpanded) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        val hasSelection = selectedCategory != null
        val selectedColor = selectedCategory?.let { getCategoryColor(it) } ?: accentColor

        Box(
            modifier = Modifier
                .size(44.dp)
                .scale(buttonScale)
                .clip(CircleShape)
                .background(
                    if (isExpanded) accentColor
                    else if (hasSelection) selectedColor.copy(alpha = 0.15f)
                    else buttonBgColor
                )
                .clickable { isExpanded = !isExpanded },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(22.dp)) {
                if (hasSelection && !isExpanded) {
                    when (selectedCategory) {
                        FocusCategory.BOOK -> drawBookIcon(selectedColor)
                        FocusCategory.WORK -> drawWorkIcon(selectedColor)
                        FocusCategory.SPORT -> drawSportIcon(selectedColor)
                        FocusCategory.COFFEE -> drawCoffeeIcon(selectedColor)
                        FocusCategory.GAMING -> drawGamingIcon(selectedColor)
                        null -> {}
                    }
                } else {
                    drawTagIcon(
                        color = if (isExpanded) Color.White else textColor.copy(alpha = 0.6f)
                    )
                }
            }
        }

        categories.forEachIndexed { index, category ->
            val itemScale by animateFloatAsState(
                targetValue = if (isExpanded) 1f else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "categoryScale$index"
            )

            val itemOffset by animateDpAsState(
                targetValue = if (isExpanded) 8.dp else (-20).dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "categoryOffset$index"
            )

            if (itemScale > 0.01f) {
                Spacer(modifier = Modifier.width(itemOffset.coerceAtLeast(0.dp)))

                val isSelected = selectedCategory == category
                val categoryColor = getCategoryColor(category)

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .scale(itemScale)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) categoryColor.copy(alpha = 0.2f)
                            else categoryBgColor
                        )
                        .clickable {
                            if (selectedCategory == category) {
                                onCategorySelected(null)
                            } else {
                                onCategorySelected(category)
                            }
                            isExpanded = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(18.dp)) {
                        when (category) {
                            FocusCategory.BOOK -> drawBookIcon(
                                color = if (isSelected) categoryColor else textColor.copy(alpha = 0.6f)
                            )
                            FocusCategory.WORK -> drawWorkIcon(
                                color = if (isSelected) categoryColor else textColor.copy(alpha = 0.6f)
                            )
                            FocusCategory.SPORT -> drawSportIcon(
                                color = if (isSelected) categoryColor else textColor.copy(alpha = 0.6f)
                            )
                            FocusCategory.COFFEE -> drawCoffeeIcon(
                                color = if (isSelected) categoryColor else textColor.copy(alpha = 0.6f)
                            )
                            FocusCategory.GAMING -> drawGamingIcon(
                                color = if (isSelected) categoryColor else textColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getCategoryColor(category: FocusCategory): Color {
    return when (category) {
        FocusCategory.BOOK -> Color(0xFF5C6BC0)
        FocusCategory.WORK -> Color(0xFF42A5F5)
        FocusCategory.SPORT -> Color(0xFFEF5350)
        FocusCategory.COFFEE -> Color(0xFF8D6E63)
        FocusCategory.GAMING -> Color(0xFF66BB6A)
    }
}

// --- Canvas Icon Drawing Functions ---

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTagIcon(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    val tagPath = Path().apply {
        moveTo(w * 0.1f, h * 0.2f)
        lineTo(w * 0.6f, h * 0.2f)
        lineTo(w * 0.9f, h * 0.5f)
        lineTo(w * 0.6f, h * 0.8f)
        lineTo(w * 0.1f, h * 0.8f)
        close()
    }
    drawPath(tagPath, color = color, style = stroke)

    drawCircle(
        color = color,
        radius = w * 0.07f,
        center = Offset(w * 0.28f, h * 0.5f),
        style = Fill
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBookIcon(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    val leftPage = Path().apply {
        moveTo(w * 0.5f, h * 0.18f)
        quadraticBezierTo(w * 0.25f, h * 0.12f, w * 0.08f, h * 0.18f)
        lineTo(w * 0.08f, h * 0.82f)
        quadraticBezierTo(w * 0.28f, h * 0.78f, w * 0.5f, h * 0.82f)
    }
    drawPath(leftPage, color = color, style = stroke)

    val rightPage = Path().apply {
        moveTo(w * 0.5f, h * 0.18f)
        quadraticBezierTo(w * 0.75f, h * 0.12f, w * 0.92f, h * 0.18f)
        lineTo(w * 0.92f, h * 0.82f)
        quadraticBezierTo(w * 0.72f, h * 0.78f, w * 0.5f, h * 0.82f)
    }
    drawPath(rightPage, color = color, style = stroke)

    drawLine(color, Offset(w * 0.5f, h * 0.18f), Offset(w * 0.5f, h * 0.82f), strokeWidth = w * 0.05f)

    drawLine(color.copy(alpha = 0.4f), Offset(w * 0.18f, h * 0.38f), Offset(w * 0.42f, h * 0.38f), strokeWidth = w * 0.03f)
    drawLine(color.copy(alpha = 0.3f), Offset(w * 0.16f, h * 0.50f), Offset(w * 0.42f, h * 0.50f), strokeWidth = w * 0.03f)
    drawLine(color.copy(alpha = 0.2f), Offset(w * 0.18f, h * 0.62f), Offset(w * 0.42f, h * 0.62f), strokeWidth = w * 0.03f)

    drawLine(color.copy(alpha = 0.4f), Offset(w * 0.58f, h * 0.38f), Offset(w * 0.82f, h * 0.38f), strokeWidth = w * 0.03f)
    drawLine(color.copy(alpha = 0.3f), Offset(w * 0.58f, h * 0.50f), Offset(w * 0.84f, h * 0.50f), strokeWidth = w * 0.03f)
    drawLine(color.copy(alpha = 0.2f), Offset(w * 0.58f, h * 0.62f), Offset(w * 0.82f, h * 0.62f), strokeWidth = w * 0.03f)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWorkIcon(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.08f, h * 0.35f),
        size = Size(w * 0.84f, h * 0.55f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.08f),
        style = stroke
    )

    val handlePath = Path().apply {
        moveTo(w * 0.3f, h * 0.35f)
        lineTo(w * 0.3f, h * 0.2f)
        lineTo(w * 0.7f, h * 0.2f)
        lineTo(w * 0.7f, h * 0.35f)
    }
    drawPath(handlePath, color = color, style = stroke)

    drawLine(color, Offset(w * 0.08f, h * 0.58f), Offset(w * 0.92f, h * 0.58f), strokeWidth = w * 0.06f)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSportIcon(color: Color) {
    val w = size.width
    val h = size.height

    drawLine(color, Offset(w * 0.2f, h * 0.5f), Offset(w * 0.8f, h * 0.5f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)

    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.05f, h * 0.25f),
        size = Size(w * 0.18f, h * 0.5f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.05f),
        style = Fill
    )

    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.77f, h * 0.25f),
        size = Size(w * 0.18f, h * 0.5f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.05f),
        style = Fill
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCoffeeIcon(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.1f, h * 0.35f),
        size = Size(w * 0.6f, h * 0.55f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.06f),
        style = stroke
    )

    drawArc(
        color = color,
        startAngle = -60f,
        sweepAngle = 120f,
        useCenter = false,
        topLeft = Offset(w * 0.62f, h * 0.4f),
        size = Size(w * 0.28f, h * 0.35f),
        style = stroke
    )

    drawLine(color, Offset(w * 0.3f, h * 0.28f), Offset(w * 0.3f, h * 0.1f), strokeWidth = w * 0.05f, cap = StrokeCap.Round)
    drawLine(color, Offset(w * 0.5f, h * 0.25f), Offset(w * 0.5f, h * 0.05f), strokeWidth = w * 0.05f, cap = StrokeCap.Round)
}


private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGamingIcon(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    val bodyPath = Path().apply {
        moveTo(w * 0.2f, h * 0.3f)
        lineTo(w * 0.8f, h * 0.3f)
        quadraticBezierTo(w * 0.95f, h * 0.3f, w * 0.95f, h * 0.5f)
        quadraticBezierTo(w * 0.95f, h * 0.75f, w * 0.75f, h * 0.75f)
        lineTo(w * 0.6f, h * 0.75f)
        lineTo(w * 0.5f, h * 0.55f)
        lineTo(w * 0.4f, h * 0.75f)
        lineTo(w * 0.25f, h * 0.75f)
        quadraticBezierTo(w * 0.05f, h * 0.75f, w * 0.05f, h * 0.5f)
        quadraticBezierTo(w * 0.05f, h * 0.3f, w * 0.2f, h * 0.3f)
        close()
    }
    drawPath(bodyPath, color = color, style = stroke)

    drawLine(color, Offset(w * 0.25f, h * 0.45f), Offset(w * 0.25f, h * 0.6f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
    drawLine(color, Offset(w * 0.18f, h * 0.525f), Offset(w * 0.32f, h * 0.525f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)

    drawCircle(color = color, radius = w * 0.04f, center = Offset(w * 0.7f, h * 0.45f), style = Fill)
    drawCircle(color = color, radius = w * 0.04f, center = Offset(w * 0.78f, h * 0.55f), style = Fill)
}
