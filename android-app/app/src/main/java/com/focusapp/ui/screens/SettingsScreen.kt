package com.focusapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusapp.ui.theme.MenilFontFamily
import com.focusapp.ui.theme.CareerFontFamily

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    // Off-white background matching main screen
    val backgroundColor = Color(0xFFFBFBFB)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Canvas(modifier = Modifier.size(32.dp)) {
                        // Draw back arrow
                        val arrowSize = size.width * 0.6f
                        val centerY = size.height / 2f
                        val startX = size.width * 0.5f
                        
                        // Arrow line
                        drawLine(
                            color = Color.Black,
                            start = Offset(startX, centerY),
                            end = Offset(startX - arrowSize, centerY),
                            strokeWidth = 3.dp.toPx()
                        )
                        
                        // Arrow head top
                        drawLine(
                            color = Color.Black,
                            start = Offset(startX - arrowSize, centerY),
                            end = Offset(startX - arrowSize + arrowSize * 0.4f, centerY - arrowSize * 0.4f),
                            strokeWidth = 3.dp.toPx()
                        )
                        
                        // Arrow head bottom
                        drawLine(
                            color = Color.Black,
                            start = Offset(startX - arrowSize, centerY),
                            end = Offset(startX - arrowSize + arrowSize * 0.4f, centerY + arrowSize * 0.4f),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(24.dp))
                
                Text(
                    text = "Settings",
                    style = TextStyle(
                        fontFamily = CareerFontFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Settings container with rounded rectangle
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Placeholder for settings options
                    Text(
                        text = "Settings options will be added here",
                        style = TextStyle(
                            fontFamily = CareerFontFamily,
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                    )
                }
            }
        }
    }
}
