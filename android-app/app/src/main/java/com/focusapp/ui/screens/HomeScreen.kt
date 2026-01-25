package com.focusapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusapp.ui.theme.MenilFontFamily
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    sessionViewModel: SessionViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToSettings: () -> Unit
) {
    // Background color - off white
    val backgroundColor = Color(0xFFFBFBFB)
    
    // Navigation state (2 = current/main screen with clock)
    var currentScreen by remember { mutableStateOf(2) }
    
    // Real-time clock
    var currentTime by remember { mutableStateOf(getCurrentTimeString()) }
    
    // Update clock every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = getCurrentTimeString()
            delay(1000)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Decorative half circle in background
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = size.height * 0.7f
            
            // Draw decorative arc (half circle)
            drawArc(
                color = Color(0xFFE5E5E5),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar with settings icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, end = 24.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text(
                        text = "⚙️",
                        style = TextStyle(fontSize = 20.sp)
                    )
                }
            }
            
            // Center clock display
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Split time and period
                val timeParts = currentTime.split("\n")
                val time = timeParts[0]
                val period = if (timeParts.size > 1) timeParts[1] else ""
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Main time display
                    Text(
                        text = time,
                        style = TextStyle(
                            fontFamily = MenilFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 160.sp,
                            lineHeight = 160.sp,
                            letterSpacing = (-3).sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    )
                    
                    // AM/PM or ÖÖ/ÖS indicator (1/5 size, positioned to the right)
                    if (period.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = period,
                            style = TextStyle(
                                fontFamily = MenilFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 32.sp,
                                lineHeight = 32.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.offset(y = 24.dp) // Align with bottom of time
                        )
                    }
                }
            }
            
            // Bottom navigation dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    NavigationDot(
                        isActive = index == currentScreen,
                        onClick = { currentScreen = index }
                    )
                    if (index < 2) {
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
            }
        }
    }
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

private fun getCurrentTimeString(): String {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    
    // Get locale to determine AM/PM or ÖÖ/ÖS
    val locale = Locale.getDefault()
    val isTurkish = locale.language == "tr"
    
    return if (isTurkish) {
        // Turkish format with ÖÖ/ÖS
        val period = if (hour < 12) "öö" else "ös"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        String.format("%02d:%02d\n%s", displayHour, minute, period)
    } else {
        // English format with AM/PM
        val period = if (hour < 12) "am" else "pm"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        String.format("%02d:%02d\n%s", displayHour, minute, period)
    }
}
