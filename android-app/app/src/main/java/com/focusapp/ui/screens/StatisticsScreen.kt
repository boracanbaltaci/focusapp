package com.focusapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusapp.data.StatisticsRepository
import java.util.*

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
    
    val data = remember(viewMode) {
        when (viewMode) {
            ViewMode.WEEK -> statisticsRepository.getWeeklyData()
            ViewMode.MONTH -> statisticsRepository.getMonthlyData()
            ViewMode.YEAR -> statisticsRepository.getYearlyData()
        }
    }
    
    val totalMinutes = statisticsRepository.getTotalMinutes(data)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    
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
            // Title
            Text(
                text = "Statistics",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // View mode selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ViewModeButton(
                    text = "Week",
                    selected = viewMode == ViewMode.WEEK,
                    onClick = { viewMode = ViewMode.WEEK },
                    textColor = textColor
                )
                ViewModeButton(
                    text = "Month",
                    selected = viewMode == ViewMode.MONTH,
                    onClick = { viewMode = ViewMode.MONTH },
                    textColor = textColor
                )
                ViewModeButton(
                    text = "Year",
                    selected = viewMode == ViewMode.YEAR,
                    onClick = { viewMode = ViewMode.YEAR },
                    textColor = textColor
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Total time
            Text(
                text = "Total: ${hours}h ${minutes}m",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bar chart
            BarChart(
                data = data,
                viewMode = viewMode,
                textColor = textColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Labels
            ChartLabels(
                data = data,
                viewMode = viewMode,
                textColor = textColor,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ViewModeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(
                color = if (selected) Color(0xFF4CAF50) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else textColor
        )
    }
}

@Composable
private fun BarChart(
    data: Map<Int, Int>,
    viewMode: ViewMode,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val maxValue = data.values.maxOrNull() ?: 1
    val barColor = Color(0xFF4CAF50)
    
    Canvas(modifier = modifier) {
        val chartWidth = size.width
        val chartHeight = size.height
        val barCount = data.size
        val barWidth = (chartWidth / barCount) * 0.7f
        val spacing = (chartWidth / barCount) * 0.3f
        
        data.entries.forEachIndexed { index, entry ->
            val barHeight = if (maxValue > 0) {
                (entry.value.toFloat() / maxValue) * chartHeight
            } else 0f
            
            val x = index * (barWidth + spacing) + spacing / 2
            val y = chartHeight - barHeight
            
            // Draw bar
            drawRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
private fun ChartLabels(
    data: Map<Int, Int>,
    viewMode: ViewMode,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        data.keys.sorted().forEach { key ->
            val label = when (viewMode) {
                ViewMode.WEEK -> {
                    when (key) {
                        1 -> "Mon"
                        2 -> "Tue"
                        3 -> "Wed"
                        4 -> "Thu"
                        5 -> "Fri"
                        6 -> "Sat"
                        7 -> "Sun"
                        else -> ""
                    }
                }
                ViewMode.MONTH -> {
                    if (key % 5 == 1 || key == data.keys.max()) key.toString() else ""
                }
                ViewMode.YEAR -> {
                    when (key) {
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
            
            Text(
                text = label,
                fontSize = 12.sp,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SettingsIconButton(
    onClick: () -> Unit,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.width / 3
            val toothCount = 6
            val toothSize = size.width / 6
            
            // Draw teeth
            for (i in 0 until toothCount) {
                val angle = (i * 360f / toothCount)
                val radian = Math.toRadians(angle.toDouble())
                val x1 = centerX + (radius * Math.cos(radian)).toFloat()
                val y1 = centerY + (radius * Math.sin(radian)).toFloat()
                val x2 = centerX + ((radius + toothSize) * Math.cos(radian)).toFloat()
                val y2 = centerY + ((radius + toothSize) * Math.sin(radian)).toFloat()
                
                drawLine(
                    color = textColor,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 3.dp.toPx()
                )
            }
            
            // Draw center circle
            drawCircle(
                color = textColor,
                radius = radius,
                center = Offset(centerX, centerY)
            )
            
            // Draw inner hole
            drawCircle(
                color = Color(0xFFFBFBFB),
                radius = radius * 0.4f,
                center = Offset(centerX, centerY)
            )
        }
    }
}
