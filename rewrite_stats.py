import os

filepath = r"c:\projects\focusapp\android-app\app\src\main\java\com\focusapp\ui\screens\StatisticsScreen.kt"

new_imports = """package com.focusapp.ui.screens

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

    val cardBg = Color(0xFFFAF2DA) 
    val barColor = Color(0xFFA5F456)
    val orangeText = Color(0xFFFF6A3D)
    val gradStart = Color(0xFF5EDCD5)
    val gradEnd = Color(0xFFA8B530)
    val blackText = Color(0xFF1E1E1E)
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        SettingsIconButton(
            onClick = onNavigateToSettings, 
            iconColor = Color.White
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp)) // padding below settings button

            // Top section: Stats, Central Card, Dropdown
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left Block
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Stats", 
                        color = Color.White, 
                        fontFamily = GeistFontFamily, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        val activeDays = data.values.count { it > 0 }
                        for (i in 0 until 5) {
                            val active = i < activeDays
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (active) barColor else cardBg)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                }

                // Center Block (Summary Card)
                Row(
                    modifier = Modifier
                        .height(72.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(cardBg)
                ) {
                    // Left White Area
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.White)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔥", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$streak days", 
                                color = orangeText, 
                                fontFamily = GeistFontFamily, 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "Streak", 
                            color = blackText, 
                            fontFamily = GeistFontFamily, 
                            fontSize = 12.sp
                        )
                    }

                    // Right Area
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(end = 20.dp, start = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$hours",
                                style = TextStyle(
                                    brush = Brush.linearGradient(colors = listOf(gradStart, gradEnd)),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = GeistFontFamily
                                )
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("hrs", color = blackText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = GeistFontFamily)
                            
                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "$minutes",
                                style = TextStyle(
                                    brush = Brush.linearGradient(colors = listOf(gradStart, gradEnd)),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = GeistFontFamily
                                )
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("mins", color = blackText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = GeistFontFamily)
                        }
                        Text(
                            text = "Total Duration", 
                            color = blackText.copy(alpha = 0.8f), 
                            fontSize = 11.sp, 
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
                            .background(Color.White)
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
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Large Box for Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardBg)
                    .padding(vertical = 16.dp, horizontal = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total", color = blackText, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = GeistFontFamily)
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
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
                                    modifier = Modifier.fillMaxWidth().weight(1f), 
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    val currentMaxHeight = maxHeight
                                    val fraction = if (maxVal > 0) (entry.value.toFloat() / maxVal).coerceIn(0f, 1f) else 0f
                                    val barH = currentMaxHeight * fraction
                                    
                                    val h = entry.value / 60
                                    val m = entry.value % 60
                                    val textAbove = when {
                                        h > 0 && m > 0 -> "${h}hr ${m} mins"
                                        h > 0 -> "${h}hr"
                                        m > 0 -> "${m} mins"
                                        else -> "0 min"
                                    }

                                    if (entry.value > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.9f)
                                                .height(barH)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(barColor)
                                        )
                                    }
                                    
                                    Text(
                                        text = textAbove,
                                        modifier = Modifier.padding(bottom = barH + 6.dp),
                                        color = blackText.copy(alpha = 0.5f),
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center,
                                        fontFamily = GeistFontFamily
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                val dayLabel = getDayLabel(entry.key, context, viewMode)
                                Text(dayLabel, color = blackText, fontSize = 14.sp, fontFamily = GeistFontFamily)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pagination dots at the very bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)))
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)))
            }
            Spacer(modifier = Modifier.height(16.dp))
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
    print("Could not find pin category selector")
else:
    remainder = text[idx:]
    final_text = new_imports + "\n" + remainder
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(final_text)
    print("Successfully updated StatisticsScreen.kt")
