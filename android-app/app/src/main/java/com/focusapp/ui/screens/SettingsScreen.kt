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
    val clockFont by settingsViewModel.clockFont.collectAsState()
    val theme by settingsViewModel.theme.collectAsState()
    
    var showFontSubmenu by remember { mutableStateOf(false) }
    
    // Theme-based colors
    val backgroundColor = if (theme == "dark") Color(0xFF181C14) else Color(0xFFFBFBFB)
    val textColor = if (theme == "dark") Color(0xFFECDFCC) else Color.Black
    val containerColor = if (theme == "dark") Color(0xFF181C14) else Color.White
    
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
                        
                        val arrowColor = Color.Black
                        
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
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (showFontSubmenu) {
                        // Font Selection Submenu
                        FontSubmenu(
                            selectedFont = clockFont,
                            onFontSelect = { 
                                settingsViewModel.setClockFont(it)
                                showFontSubmenu = false
                            },
                            onBack = { showFontSubmenu = false },
                            textColor = textColor
                        )
                    } else {
                        // Main Settings Menu
                        
                        // Clock Font Setting (clickable to open submenu)
                        ClickableSettingItem(
                            title = stringResource(R.string.clock_font),
                            subtitle = if (clockFont == "menil") stringResource(R.string.font_menil) else stringResource(R.string.font_avocado),
                            onClick = { showFontSubmenu = true },
                            textColor = textColor
                        )
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = textColor.copy(alpha = 0.1f)
                        )
                        
                        // Theme Setting with Switch
                        ThemeSettingItem(
                            isDark = theme == "dark",
                            onThemeChange = { isDark ->
                                settingsViewModel.setTheme(if (isDark) "dark" else "light")
                            },
                            textColor = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClickableSettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = CareerFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = textColor
                )
            )
            Text(
                text = subtitle,
                style = TextStyle(
                    fontFamily = CareerFontFamily,
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
            )
        }
        
        // Chevron arrow
        Canvas(modifier = Modifier.size(20.dp)) {
            val arrowColor = textColor.copy(alpha = 0.6f)
            val centerY = size.height / 2f
            val arrowSize = size.width * 0.5f
            
            drawLine(
                color = arrowColor,
                start = Offset(0f, centerY - arrowSize / 2f),
                end = Offset(arrowSize, centerY),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = arrowColor,
                start = Offset(arrowSize, centerY),
                end = Offset(0f, centerY + arrowSize / 2f),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

@Composable
private fun ThemeSettingItem(
    isDark: Boolean,
    onThemeChange: (Boolean) -> Unit,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Theme",
            style = TextStyle(
                fontFamily = CareerFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = textColor
            )
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isDark) "Dark" else "Light",
                style = TextStyle(
                    fontFamily = CareerFontFamily,
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
            )
            
            Switch(
                checked = isDark,
                onCheckedChange = onThemeChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF4CAF50),
                    checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun FontSubmenu(
    selectedFont: String,
    onFontSelect: (String) -> Unit,
    onBack: () -> Unit,
    textColor: Color
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Back button
        Row(
            modifier = Modifier
                .clickable(onClick = onBack)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Canvas(modifier = Modifier.size(20.dp)) {
                val arrowColor = textColor
                val centerY = size.height / 2f
                val arrowSize = size.width * 0.6f
                
                drawLine(
                    color = arrowColor,
                    start = Offset(size.width * 0.5f, centerY),
                    end = Offset(size.width * 0.5f - arrowSize, centerY),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = arrowColor,
                    start = Offset(size.width * 0.5f - arrowSize, centerY),
                    end = Offset(size.width * 0.5f - arrowSize + arrowSize * 0.4f, centerY - arrowSize * 0.4f),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = arrowColor,
                    start = Offset(size.width * 0.5f - arrowSize, centerY),
                    end = Offset(size.width * 0.5f - arrowSize + arrowSize * 0.4f, centerY + arrowSize * 0.4f),
                    strokeWidth = 2.dp.toPx()
                )
            }
            
            Text(
                text = "Font Selection",
                style = TextStyle(
                    fontFamily = CareerFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = textColor
                )
            )
        }
        
        Divider(color = textColor.copy(alpha = 0.1f))
        
        // Font options
        val fontOptions = listOf(
            "Menil-Étroit" to "menil",
            "LT Avocado" to "avocado"
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            fontOptions.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            role = Role.RadioButton,
                            onClick = { onFontSelect(value) }
                        )
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = TextStyle(
                            fontFamily = CareerFontFamily,
                            fontSize = 16.sp,
                            color = textColor.copy(alpha = if (selectedFont == value) 1f else 0.6f),
                            fontWeight = if (selectedFont == value) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                    
                    if (selectedFont == value) {
                        // Checkmark indicator
                        val checkColor = Color(0xFF4CAF50)
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
                        // Checkmark indicator (green)
                        val checkColor = Color(0xFF4CAF50)
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

