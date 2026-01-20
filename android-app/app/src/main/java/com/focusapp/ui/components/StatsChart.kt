package com.focusapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun StatsChart(stats: Map<String, Long>) {
    val maxValue = stats.values.maxOrNull() ?: 1L
    val barColor = Color(0xFF4A90E2)
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp)
        ) {
            val barWidth = size.width / stats.size.coerceAtLeast(1)
            val maxHeight = size.height
            
            stats.entries.forEachIndexed { index, entry ->
                val barHeight = (entry.value.toFloat() / maxValue.toFloat()) * maxHeight
                
                drawRect(
                    color = barColor,
                    topLeft = Offset(index * barWidth + barWidth * 0.1f, maxHeight - barHeight),
                    size = Size(barWidth * 0.8f, barHeight)
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            stats.keys.take(7).forEach { date ->
                val dayOfWeek = date.takeLast(2)
                Text(
                    text = dayOfWeek,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}
