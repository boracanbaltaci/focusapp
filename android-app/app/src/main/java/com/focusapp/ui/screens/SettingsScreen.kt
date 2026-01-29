package com.focusapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusapp.R
import com.focusapp.ui.theme.MenilFontFamily
import com.focusapp.ui.theme.CareerFontFamily

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val theme by settingsViewModel.theme.collectAsState()
    val clockFont by settingsViewModel.clockFont.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    
    // Theme colors
    val backgroundColor = if (theme == "dark") Color(0xFF181C14) else Color(0xFFFBFBFB)
    val textColor = if (theme == "dark") Color(0xFFECDFCC) else Color.Black
    val containerColor = if (theme == "dark") Color(0xFF1F2419) else Color.White
    
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
                        
                        val arrowColor = if (theme == "dark") Color(0xFFECDFCC) else Color.Black
                        
                        // Arrow line
                        drawLine(
                            color = arrowColor,
                            start = Offset(startX, centerY),
                            end = Offset(startX - arrowSize, centerY),
                            strokeWidth = 3.dp.toPx()
                        )
                        
                        // Arrow head top
                        drawLine(
                            color = arrowColor,
                            start = Offset(startX - arrowSize, centerY),
                            end = Offset(startX - arrowSize + arrowSize * 0.4f, centerY - arrowSize * 0.4f),
                            strokeWidth = 3.dp.toPx()
                        )
                        
                        // Arrow head bottom
                        drawLine(
                            color = arrowColor,
                            start = Offset(startX - arrowSize, centerY),
                            end = Offset(startX - arrowSize + arrowSize * 0.4f, centerY + arrowSize * 0.4f),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(24.dp))
                
                Text(
                    text = stringResource(R.string.settings),
                    style = TextStyle(
                        fontFamily = CareerFontFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Normal,
                        color = textColor
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
                color = containerColor,
                shadowElevation = if (theme == "dark") 0.dp else 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Clock Font Setting
                    SettingItem(
                        title = stringResource(R.string.clock_font),
                        options = listOf(
                            stringResource(R.string.font_menil) to "menil",
                            stringResource(R.string.font_avocado) to "avocado"
                        ),
                        selectedValue = clockFont,
                        onValueChange = { settingsViewModel.setClockFont(it) },
                        textColor = textColor
                    )
                    
                    Divider(color = textColor.copy(alpha = 0.2f))
                    
                    // Language Setting
                    SettingItem(
                        title = stringResource(R.string.language),
                        options = listOf(
                            stringResource(R.string.lang_english) to "en",
                            stringResource(R.string.lang_turkish) to "tr",
                            stringResource(R.string.lang_french) to "fr",
                            stringResource(R.string.lang_spanish) to "es",
                            stringResource(R.string.lang_italian) to "it",
                            stringResource(R.string.lang_german) to "de"
                        ),
                        selectedValue = language,
                        onValueChange = { settingsViewModel.setLanguage(it) },
                        textColor = textColor
                    )
                    
                    Divider(color = textColor.copy(alpha = 0.2f))
                    
                    // Theme Setting
                    SettingItem(
                        title = stringResource(R.string.theme),
                        options = listOf(
                            stringResource(R.string.light) to "light",
                            stringResource(R.string.dark) to "dark"
                        ),
                        selectedValue = theme,
                        onValueChange = { settingsViewModel.setTheme(it) },
                        textColor = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    options: List<Pair<String, String>>,
    selectedValue: String,
    onValueChange: (String) -> Unit,
    textColor: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = CareerFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = textColor
            )
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            role = Role.RadioButton,
                            onClick = { onValueChange(value) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = TextStyle(
                            fontFamily = CareerFontFamily,
                            fontSize = 16.sp,
                            color = textColor.copy(alpha = if (selectedValue == value) 1f else 0.6f),
                            fontWeight = if (selectedValue == value) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                    
                    if (selectedValue == value) {
                        // Checkmark indicator - theme aware
                        val checkColor = if (textColor.luminance() > 0.5f) {
                            Color(0xFF4CAF50) // Green for light theme
                        } else {
                            Color(0xFF81C784) // Lighter green for dark theme
                        }
                        Canvas(modifier = Modifier.size(20.dp)) {
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width * 0.2f, size.height * 0.5f)
                                lineTo(size.width * 0.4f, size.height * 0.7f)
                                lineTo(size.width * 0.8f, size.height * 0.2f)
                            }
                            drawPath(
                                path = path,
                                color = checkColor,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 3.dp.toPx(),
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

