import os

filepath = r"c:\projects\focusapp\android-app\app\src\main\java\com\focusapp\ui\screens\StatisticsScreen.kt"

new_code = """package com.focusapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val streak = remember(refreshTick) { statisticsRepository.getCurrentStreak() }

    val isDark = textColor != Color.Black
    val cardBg = Color(0xFFFAF2DA) 
    val barColor = Color(0xFFA5F456)
    val orangeText = Color(0xFFFF6A3D)
    val gradStart = Color(0xFF5EDCD5)
    val gradEnd = Color(0xFFA8B530)
    val blackText = Color(0xFF1E1E1E)
    val summaryCardWhite = Color.White
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        SettingsIconButton(
            onClick = onNavigateToSettings, 
            iconColor = textColor
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 24.dp)
        ) {
            // Provide space for settings icon to avoid overlap
            Spacer(modifier = Modifier.height(26.dp))
            
            // Top section: Stats, Central Card, Dropdown
            // Removed internal spacer to make it sit higher and give more room to chart
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left Block
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Stats", 
                        color = textColor, 
                        fontFamily = GeistFontFamily, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row {
                        val activeDays = data.values.count { it > 0 }
                        for (i in 0 until 5) {
                            val active = i < activeDays
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (active) barColor else Color.White)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                }

                // Center Block (Summary Card)
                Row(
                    modifier = Modifier
                        .height(80.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(cardBg)
                ) {
                    // Left White Area
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(26.dp))
                            .background(summaryCardWhite)
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔥", fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$streak days", 
                            color = orangeText, 
                            fontFamily = GeistFontFamily, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Streak", 
                            color = blackText.copy(alpha = 0.8f), 
                            fontFamily = GeistFontFamily, 
                            fontSize = 11.sp
                        )
                    }

                    // Right Area
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 12.dp, end = 16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.Baseline) {
                            Text(
                                text = "$hours",
                                style = TextStyle(
                                    brush = Brush.linearGradient(colors = listOf(gradStart, gradEnd)),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = GeistFontFamily
                                )
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("hrs", color = blackText, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = GeistFontFamily)
                        }
                        Row(verticalAlignment = Alignment.Baseline) {
                            Text(
                                text = "$minutes",
                                style = TextStyle(
                                    brush = Brush.linearGradient(colors = listOf(gradStart, gradEnd)),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = GeistFontFamily
                                )
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("mins", color = blackText, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = GeistFontFamily)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Total Duration", 
                            color = blackText.copy(alpha = 0.7f), 
                            fontSize = 10.sp, 
                            fontFamily = GeistFontFamily
                        )
                    }
                }

                // Right Block
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color.White else Color(0xFFE0E0E0))
                            .clickable {
                                viewMode = when (viewMode) {
                                    ViewMode.WEEK -> ViewMode.MONTH
                                    ViewMode.MONTH -> ViewMode.YEAR
                                    ViewMode.YEAR -> ViewMode.WEEK
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val modeText = when (viewMode) {
                            ViewMode.WEEK -> "Weekly"
                            ViewMode.MONTH -> "Monthly"
                            ViewMode.YEAR -> "Yearly"
                        }
                        Text(modeText, color = blackText, fontSize = 12.sp, fontFamily = GeistFontFamily)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.KeyboardArrowDown, 
                            contentDescription = null, 
                            tint = blackText, 
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Large Box for Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(cardBg)
                    .padding(top = 16.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(), 
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Total", 
                        color = blackText, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 16.sp, 
                        fontFamily = GeistFontFamily
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val sortedEntries = data.entries.sortedBy { it.key }
                        val maxVal = (sortedEntries.maxOfOrNull { it.value } ?: 1).coerceAtLeast(1)

                        // Draw maximum 7 columns
                        sortedEntries.take(7).forEach { entry ->
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                BoxWithConstraints(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f), 
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    val barAreaHeight = maxHeight - 30.dp
                                    val fraction = if (maxVal > 0) (entry.value.toFloat() / maxVal).coerceIn(0f, 1f) else 0f
                                    val barH = barAreaHeight * fraction
                                    
                                    val h = entry.value / 60
                                    val m = entry.value % 60
                                    val textAbove = when {
                                        h > 0 && m > 0 -> "${h}hr\\n${m}m"
                                        h > 0 -> "${h}hr"
                                        m > 0 -> "${m}m"
                                        else -> "0 m"
                                    }

                                    if (entry.value > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.85f)
                                                .height(barH.coerceAtLeast(4.dp))
                                                .align(Alignment.BottomCenter)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(barColor)
                                        )
                                    }
                                    
                                    Text(
                                        text = textAbove,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = barH + 6.dp),
                                        color = blackText.copy(alpha = 0.5f),
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp,
                                        maxLines = 2,
                                        textAlign = TextAlign.Center,
                                        fontFamily = GeistFontFamily
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                val dayLabel = getDayLabel(entry.key, context, viewMode)
                                Text(
                                    dayLabel, 
                                    color = blackText.copy(alpha = 0.8f), 
                                    fontSize = 14.sp, 
                                    fontFamily = GeistFontFamily
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Pagination dots at the very bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(textColor.copy(alpha = 0.8f)))
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(textColor.copy(alpha = 0.2f)))
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(textColor.copy(alpha = 0.2f)))
            }
            Spacer(modifier = Modifier.height(10.dp))
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
"""

with open(filepath, "r", encoding="utf-8") as f:
    text = f.read()
    
idx = text.find("// --- Pin Category Selector ---")
if idx == -1:
    print("Could not find bottom")
else:
    remainder = text[idx:]
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(new_code + "\n" + remainder)
    print("Successfully replaced proportion script.")
