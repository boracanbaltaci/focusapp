package com.focusapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clockera.R
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
    var selectedCategoryFilter by remember { mutableStateOf<FocusCategory?>(null) }

    var refreshTick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            refreshTick++
        }
    }

    val data = remember(viewMode, refreshTick, selectedCategoryFilter) {
        val filterStr = selectedCategoryFilter?.name
        when (viewMode) {
            ViewMode.WEEK -> statisticsRepository.getWeeklyData(filterStr)
            ViewMode.MONTH -> statisticsRepository.getMonthlyData(filterStr)
            ViewMode.YEAR -> statisticsRepository.getYearlyData(filterStr)
        }
    }

    val prevData = remember(viewMode, refreshTick, selectedCategoryFilter) {
        val filterStr = selectedCategoryFilter?.name
        when (viewMode) {
            ViewMode.WEEK -> statisticsRepository.getPreviousWeeklyData(filterStr)
            ViewMode.MONTH -> statisticsRepository.getPreviousMonthlyData(filterStr)
            ViewMode.YEAR -> statisticsRepository.getPreviousYearlyData(filterStr)
        }
    }

    val totalMinutes = statisticsRepository.getTotalMinutes(data)
    val prevTotalMinutes = statisticsRepository.getTotalMinutes(prevData)

    val trendPercent = if (prevTotalMinutes > 0) {
        ((totalMinutes - prevTotalMinutes).toFloat() / prevTotalMinutes * 100).toInt()
    } else if (totalMinutes > 0) {
        100
    } else {
        0
    }
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val streak = remember(refreshTick) { statisticsRepository.getCurrentStreak() }
    
    // Derived values for bottom cards
    val totalSessions = remember(viewMode, refreshTick) {
        statisticsRepository.getAllSessions().size
    }
    
    val rawHours = totalMinutes / 60f
    val dailyAvgText = String.format(java.util.Locale.US, "%.1f Hrs", if (data.isNotEmpty()) rawHours / data.size.coerceAtLeast(1) else 0f)

    var showStatsPopup by remember { mutableStateOf(false) }
    var showTagsPopup by remember { mutableStateOf(false) }
    var tooltipEntry by remember { mutableStateOf<Map.Entry<Int, StatisticsRepository.DayStats>?>(null) }

    val isDark = textColor != Color.Black
    
    val bgTabContainer = Color(0xFF2F3033)
    val bgTabActive = Color(0xFF0DF259)
    val textTabActive = Color(0xFF102216)
    val textTabInactive = Color(0xFF64748B)
    val cardBg = bgTabContainer
    val barColor = bgTabActive

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(showStatsPopup, showTagsPopup) {
                if (showStatsPopup || showTagsPopup) {
                    detectTapGestures {
                        showStatsPopup = false
                        showTagsPopup = false
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 64.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // --- HEADER ---
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Title & Subtitle
                Column(modifier = Modifier.align(Alignment.CenterStart)) {
                    Text(
                        text = "Analytics",
                        color = textColor, 
                        fontFamily = GeistFontFamily, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 28.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Visualizing your path to ultimate productivity.",
                        color = textTabInactive,
                        fontFamily = GeistFontFamily,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(modifier = Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalTabGroup(
                        viewMode = viewMode,
                        onViewModeSelected = { viewMode = it },
                        bgContainer = Color(0xFF1B2A20),
                        bgActive = bgTabActive,
                        textActive = textTabActive,
                        textInactive = textTabInactive
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(onClick = { showTagsPopup = !showTagsPopup; showStatsPopup = false }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.List, contentDescription = "Tags", tint = textColor)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { showStatsPopup = !showStatsPopup; showTagsPopup = false }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Star, contentDescription = "Stats", tint = textColor)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // --- MAIN CHART CARD ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) 
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg.copy(alpha = 0.5f))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Card Top Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text("TOTAL FOCUS TIME", color = textTabInactive, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = GeistFontFamily)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${hours}h ${minutes}m", color = textColor, fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = GeistFontFamily)
                                Spacer(modifier = Modifier.width(12.dp))
                                // Trend Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0F3D1F))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (trendPercent == 0) "0%" else if (trendPercent > 0) "↗ +$trendPercent%" else "↘ $trendPercent%", 
                                        color = if (trendPercent == 0) Color.Gray else if (trendPercent > 0) bgTabActive else Color(0xFFFF5252), 
                                        fontSize = 10.sp, 
                                        fontWeight = FontWeight.Bold, 
                                        fontFamily = GeistFontFamily
                                    )
                                }
                            }
                        }
                        
                        // Legend
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(bgTabActive))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("FOCUS", color = textColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = GeistFontFamily)
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFF9800))) // Orange
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("BREAK", color = textColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = GeistFontFamily)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Bar Chart
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val sortedEntries = data.entries.sortedBy { it.key }
                            val maxVal = (sortedEntries.maxOfOrNull { it.value.focusMinutes + it.value.breakMinutes } ?: 1).coerceAtLeast(1)

                            val displayEntries = sortedEntries
                            val barCount = displayEntries.size
                            
                            displayEntries.forEach { entry ->
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    BoxWithConstraints(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .pointerInput(entry.key) {
                                                detectTapGestures(
                                                    onLongPress = { 
                                                        tooltipEntry = entry 
                                                    },
                                                    onPress = {
                                                        if (tryAwaitRelease()) {
                                                            tooltipEntry = null
                                                        }
                                                    }
                                                )
                                            },
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        val barAreaHeight = maxHeight
                                        
                                        val focusFraction = if (maxVal > 0) (entry.value.focusMinutes.toFloat() / maxVal).coerceIn(0f, 1f) else 0f
                                        val breakFraction = if (maxVal > 0) (entry.value.breakMinutes.toFloat() / maxVal).coerceIn(0f, 1f) else 0f
                                        
                                        val focusH = barAreaHeight * focusFraction
                                        val breakH = barAreaHeight * breakFraction
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(if (barCount > 15) 0.9f else 0.7f),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            // Focus Bar
                                            if (entry.value.focusMinutes > 0 || focusFraction > 0f) { 
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(focusH.coerceAtLeast(4.dp))
                                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                        .background(bgTabActive) 
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                            
                                            Spacer(modifier = Modifier.width(2.dp))
                                            
                                            // Break Bar
                                            if (entry.value.breakMinutes > 0 || breakFraction > 0f) { 
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(breakH.coerceAtLeast(4.dp))
                                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                        .background(Color(0xFFFF9800)) 
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                    
                                    Box(modifier = Modifier.fillMaxWidth().height(24.dp), contentAlignment = Alignment.Center) {
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = tooltipEntry?.key == entry.key
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.White)
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("${entry.value.focusMinutes}m", color = Color(0xFF102216), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    Text("${entry.value.breakMinutes}m", color = Color(0xFFE65100), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Label under bar
                                    val label = getDayLabel(entry.key, context, viewMode)
                                    if (barCount <= 12 || entry.key % 5 == 0 || entry.key == 1) {
                                        Text(
                                            label.take(3).uppercase(java.util.Locale.getDefault()),
                                            color = textTabInactive,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = GeistFontFamily
                                        )
                                    } else {
                                        Text(" ", fontSize = 11.sp, fontFamily = GeistFontFamily) // maintain height
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        // Popups
        if (showStatsPopup) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .width(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardBg)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Total Sessions
                    StatCard(
                        modifier = Modifier.fillMaxWidth().height(84.dp),
                        cardBg = Color(0xFF0F2615),
                        iconBg = Color(0xFF1B4D25),
                        iconColor = bgTabActive,
                        icon = Icons.Default.CheckCircle,
                        title = "TOTAL SESSIONS",
                        value = totalSessions.toString()
                    )
                    // Streak 
                    StatCard(
                        modifier = Modifier.fillMaxWidth().height(84.dp),
                        cardBg = Color(0xFF2A1C0F),
                        iconBg = Color(0xFF4A301A),
                        iconColor = Color(0xFFFF9800),
                        icon = Icons.Default.Star,
                        title = "LONGEST STREAK",
                        value = "$streak Days"
                    )
                    // Daily Average
                    StatCard(
                        modifier = Modifier.fillMaxWidth().height(84.dp),
                        cardBg = Color(0xFF1E1C28),
                        iconBg = Color(0xFF382F4C),
                        iconColor = Color(0xFFB388FF),
                        icon = Icons.Default.DateRange,
                        title = "DAILY AVERAGE",
                        value = dailyAvgText
                    )
                }
            }
        }

        if (showTagsPopup) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 64.dp, top = 80.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Column(
                    modifier = Modifier
                        .width(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Categories", color = textColor, fontWeight = FontWeight.Bold, fontFamily = GeistFontFamily, fontSize = 14.sp)
                    Divider(color = textTabInactive.copy(alpha = 0.2f))
                    
                    val tags = listOf(
                        Triple(FocusCategory.BOOK, "Öğrenme", getCategoryColor(FocusCategory.BOOK)),
                        Triple(FocusCategory.WORK, "Çalışma", getCategoryColor(FocusCategory.WORK)),
                        Triple(FocusCategory.SPORT, "Spor", getCategoryColor(FocusCategory.SPORT)),
                        Triple(FocusCategory.COFFEE, "Rahatlama", getCategoryColor(FocusCategory.COFFEE)),
                        Triple(FocusCategory.GAMING, "Oyun", getCategoryColor(FocusCategory.GAMING))
                    )
                    
                    tags.forEach { (cat, label, col) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isSelected = selectedCategoryFilter == cat
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) col.copy(alpha = 0.3f) else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(12.dp)) {
                                    val iconColor = if (isSelected) col else textTabInactive
                                    when (cat) {
                                        FocusCategory.BOOK -> drawBookIcon(iconColor)
                                        FocusCategory.WORK -> drawWorkIcon(iconColor)
                                        FocusCategory.SPORT -> drawSportIcon(iconColor)
                                        FocusCategory.COFFEE -> drawCoffeeIcon(iconColor)
                                        FocusCategory.GAMING -> drawGamingIcon(iconColor)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, color = if (isSelected) textColor else textTabInactive, fontSize = 14.sp, fontFamily = GeistFontFamily)
                        }
                    }
                }
            }
        }

        SettingsIconButton(
            onClick = onNavigateToSettings, 
            iconColor = textColor
        )
    }
}

@Composable
fun HorizontalTabGroup(
    viewMode: ViewMode,
    onViewModeSelected: (ViewMode) -> Unit,
    bgContainer: Color,
    bgActive: Color,
    textActive: Color,
    textInactive: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgContainer)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val modes = listOf(ViewMode.WEEK, ViewMode.MONTH, ViewMode.YEAR)
        val modeLabels = listOf("Weekly", "Monthly", "Yearly")
        
        modes.forEachIndexed { index, mode ->
            val isSelected = viewMode == mode
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) bgActive else Color.Transparent)
                    .clickable { onViewModeSelected(mode) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = modeLabels[index],
                    color = if (isSelected) textActive else textInactive,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = GeistFontFamily
                )
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    cardBg: Color,
    iconBg: Color,
    iconColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(verticalArrangement = Arrangement.Center) {
            Text(title, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = GeistFontFamily)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, fontFamily = GeistFontFamily)
        }
    }
}

private fun getDayLabel(key: Int, context: android.content.Context, viewMode: ViewMode): String {
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
        ViewMode.MONTH -> key.toString()
        ViewMode.YEAR -> when (key) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
            else -> ""
        }
    }
}

// --- RESTORED COMPONENTS FOR OTHER SCREENS ---

enum class FocusCategory {
    BOOK, WORK, SPORT, COFFEE, GAMING
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
        modifier = modifier.height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        val hasSelection = selectedCategory != null
        val selectedColor = selectedCategory?.let { getCategoryColor(it) } ?: accentColor

        Box(
            modifier = Modifier
                .size(24.dp)
                .scale(buttonScale)
                .clip(CircleShape)
                .background(
                    if (isExpanded) accentColor
                    else if (hasSelection) selectedColor.copy(alpha = 0.5f)
                    else Color.White
                )
                .clickable { isExpanded = !isExpanded },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(12.dp)) {
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
                label = "categoryScale${index}"
            )

            val itemOffset by animateDpAsState(
                targetValue = if (isExpanded) 6.dp else (-10).dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "categoryOffset${index}"
            )

            if (itemScale > 0.01f) {
                Spacer(modifier = Modifier.width(itemOffset.coerceAtLeast(0.dp)))

                val isSelected = selectedCategory == category
                val categoryColor = getCategoryColor(category)

                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .scale(itemScale)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) categoryColor.copy(alpha = 0.4f)
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
                    Canvas(modifier = Modifier.size(10.dp)) {
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
