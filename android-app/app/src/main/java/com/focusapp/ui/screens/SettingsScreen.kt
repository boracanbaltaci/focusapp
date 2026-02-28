package com.focusapp.ui.screens

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clockera.R
import com.focusapp.ui.components.LocalScreenScale
import com.focusapp.ui.components.scaled
import com.focusapp.ui.theme.MenilFontFamily
import com.focusapp.ui.theme.GeistFontFamily
import androidx.compose.ui.res.painterResource
import java.util.*

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    val clockFont by settingsViewModel.clockFont.collectAsState()
    val theme by settingsViewModel.theme.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val autoBreakEnabled by settingsViewModel.autoBreakEnabled.collectAsState()
    val breakDurationMinutes by settingsViewModel.breakDurationMinutes.collectAsState()
    val is24HourFormat by settingsViewModel.is24HourFormat.collectAsState()
    val pomodoroSessions by settingsViewModel.pomodoroSessions.collectAsState()
    
    var showFontSubmenu by remember { mutableStateOf(false) }
    var showLanguageSubmenu by remember { mutableStateOf(false) }
    var showBreakSubmenu by remember { mutableStateOf(false) }
    var showAboutSubmenu by remember { mutableStateOf(false) }
    var showRewardsSubmenu by remember { mutableStateOf(false) }
    var showSessionsSubmenu by remember { mutableStateOf(false) }
    var pendingLanguageChange by remember { mutableStateOf<String?>(null) }
    
    // Handle language change with LaunchedEffect for safe recreation
    LaunchedEffect(pendingLanguageChange) {
        pendingLanguageChange?.let { newLanguage ->
            try {
                updateLocale(context, newLanguage)
                activity?.recreate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            pendingLanguageChange = null
        }
    }
    
    // Theme-based colors
    val backgroundColor = if (theme == "dark") Color(0xFF181C14) else Color(0xFFFBFBFB)
    val textColor = if (theme == "dark") Color(0xFFECDFCC) else Color.Black
    val containerColor = if (theme == "dark") Color(0xFF181C14) else Color.White
    
    val settingsScale = LocalScreenScale.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp.scaled(settingsScale, min = 12.dp)),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Determine active title and back action
            val (headerTitle, onBackAction) = when {
                showFontSubmenu -> stringResource(R.string.font_selection) to { showFontSubmenu = false }
                showLanguageSubmenu -> stringResource(R.string.language) to { showLanguageSubmenu = false }
                showBreakSubmenu -> stringResource(R.string.auto_break) to { showBreakSubmenu = false }
                showAboutSubmenu -> stringResource(R.string.about) to { showAboutSubmenu = false }
                showRewardsSubmenu -> stringResource(R.string.milestones) to { showRewardsSubmenu = false }
                showSessionsSubmenu -> stringResource(R.string.sessions) to { showSessionsSubmenu = false }
                else -> stringResource(R.string.settings) to onBack
            }

            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onBackAction,
                    modifier = Modifier.size(32.dp)
                ) {
                    Canvas(modifier = Modifier.size(24.dp)) {
                        // Draw back arrow
                        val arrowSize = size.width * 0.6f
                        val centerY = size.height / 2f
                        val startX = size.width * 0.5f
                        
                        val arrowColor = textColor
                        
                        // Arrow line
                        drawLine(
                            color = arrowColor,
                            start = Offset(startX, centerY),
                            end = Offset(startX - arrowSize, centerY),
                            strokeWidth = 2.dp.toPx()
                        )
                        
                        // Arrow head top
                        drawLine(
                            color = arrowColor,
                            start = Offset(startX - arrowSize, centerY),
                            end = Offset(startX - arrowSize + arrowSize * 0.4f, centerY - arrowSize * 0.4f),
                            strokeWidth = 2.dp.toPx()
                        )
                        
                        // Arrow head bottom
                        drawLine(
                            color = arrowColor,
                            start = Offset(startX - arrowSize, centerY),
                            end = Offset(startX - arrowSize + arrowSize * 0.4f, centerY + arrowSize * 0.4f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = headerTitle,
                    style = TextStyle(
                        fontFamily = GeistFontFamily,
                        fontSize = 22.sp.scaled(settingsScale, min = 16.sp),
                        fontWeight = FontWeight.Normal,
                        color = textColor
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Settings container with rounded rectangle
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = containerColor,
                shadowElevation = 1.dp
            ) {
                // Determine current submenu key for scroll state reset
                val currentSubmenuKey = when {
                    showFontSubmenu -> "font"
                    showLanguageSubmenu -> "language"
                    showBreakSubmenu -> "break"
                    showAboutSubmenu -> "about"
                    showRewardsSubmenu -> "rewards"
                    showSessionsSubmenu -> "sessions"
                    else -> "main"
                }
                val scrollState = rememberScrollState()
                
                // Reset scroll to top when submenu changes
                LaunchedEffect(currentSubmenuKey) {
                    scrollState.scrollTo(0)
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp.scaled(settingsScale, min = 8.dp)),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
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
                    } else if (showLanguageSubmenu) {
                        // Language Selection Submenu
                        LanguageSubmenu(
                            selectedLanguage = language,
                            onLanguageSelect = {
                                settingsViewModel.setLanguage(it)
                                showLanguageSubmenu = false  // Close submenu first
                                pendingLanguageChange = it  // Trigger recreation via LaunchedEffect
                            },
                            onBack = { showLanguageSubmenu = false },
                            textColor = textColor
                        )
                    } else if (showBreakSubmenu) {
                        // Break Settings Submenu
                        BreakSubmenu(
                            autoBreakEnabled = autoBreakEnabled,
                            breakDurationMinutes = breakDurationMinutes,
                            onAutoBreakChange = { settingsViewModel.setAutoBreakEnabled(it) },
                            onDurationChange = { settingsViewModel.setBreakDurationMinutes(it) },
                            onBack = { showBreakSubmenu = false },
                            textColor = textColor
                        )

                    } else if (showAboutSubmenu) {
                        // About Page
                        AboutSubmenu(
                            onBack = { showAboutSubmenu = false },
                            textColor = textColor,
                            isDark = theme == "dark"
                        )
                    } else if (showRewardsSubmenu) {
                        // Rewards Grid
                        RewardsSubmenu(
                            textColor = textColor,
                            isDark = theme == "dark"
                        )
                    } else if (showSessionsSubmenu) {
                        // Sessions Selector
                        SessionsSubmenu(
                            currentSessions = pomodoroSessions,
                            onSessionsChange = { settingsViewModel.setPomodoroSessions(it) },
                            textColor = textColor
                        )
                    } else {
                        // Main Settings Menu
                        
                        // Clock Font Setting (clickable to open submenu)
                        ClickableSettingItem(
                            title = stringResource(R.string.clock_font),
                            subtitle = "",
                            onClick = { showFontSubmenu = true },
                            textColor = textColor
                        )
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 1.dp),
                            color = textColor.copy(alpha = 0.1f)
                        )

                        // 24-Hour Format Setting
                        SwitchSettingItem(
                            title = stringResource(R.string.format_24_hour),
                            isChecked = is24HourFormat,
                            onCheckedChange = { settingsViewModel.setIs24HourFormat(it) },
                            textColor = textColor
                        )
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 1.dp),
                            color = textColor.copy(alpha = 0.1f)
                        )
                        
                        ThemeSettingItem(
                            isDark = theme == "dark",
                            onThemeChange = { isDark ->
                                settingsViewModel.setTheme(if (isDark) "dark" else "light")
                            },
                            textColor = textColor
                        )
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 1.dp),
                            color = textColor.copy(alpha = 0.1f)
                        )
                        
                        // Language Setting (clickable to open submenu)
                        ClickableSettingItem(
                            title = stringResource(R.string.language),
                            subtitle = getLanguageDisplayName(language),
                            onClick = { showLanguageSubmenu = true },
                            textColor = textColor
                        )
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 1.dp),
                            color = textColor.copy(alpha = 0.1f)
                        )
                        
                        // Auto Break Setting (clickable to open submenu)
                        ClickableSettingItem(
                            title = stringResource(R.string.auto_break),
                            subtitle = if (autoBreakEnabled) "$breakDurationMinutes ${stringResource(R.string.break_min_suffix)}" else stringResource(R.string.break_off),
                            onClick = { showBreakSubmenu = true },
                            textColor = textColor
                        )
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 1.dp),
                            color = textColor.copy(alpha = 0.1f)
                        )

                        // Sessions Setting
                        ClickableSettingItem(
                            title = stringResource(R.string.sessions),
                            subtitle = String.format(stringResource(R.string.sessions_count), pomodoroSessions),
                            onClick = { showSessionsSubmenu = true },
                            textColor = textColor
                        )
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 1.dp),
                            color = textColor.copy(alpha = 0.1f)
                        )
                        
                        // Rewards
                        ClickableSettingItem(
                            title = stringResource(R.string.milestones),
                            subtitle = "",
                            onClick = { showRewardsSubmenu = true },
                            textColor = textColor
                        )
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 1.dp),
                            color = textColor.copy(alpha = 0.1f)
                        )
                        
                        // About
                        ClickableSettingItem(
                            title = stringResource(R.string.about),
                            subtitle = "",
                            onClick = { showAboutSubmenu = true },
                            textColor = textColor
                        )
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = textColor.copy(alpha = 0.1f)
                        )
                        
                        // Subscription
                        ClickableSettingItem(
                            title = stringResource(R.string.subscription),
                            subtitle = "",
                            onClick = { /* TODO: open subscription page */ },
                            textColor = textColor
                        )
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 1.dp),
                            color = textColor.copy(alpha = 0.1f)
                        )
                        
                        // TEST: Notification test button (temporary)
                        ClickableSettingItem(
                            title = "🔔 Test Notifications",
                            subtitle = "Send all notifications now",
                            onClick = {
                                // Inactivity notification
                                com.focusapp.notification.NotificationHelper.showNotification(
                                    context,
                                    context.getString(R.string.notif_inactivity_title),
                                    context.getString(R.string.notif_inactivity_body),
                                    1001
                                )
                                // Weekly summary notification (example: 3h 47min)
                                val timeText = "3 ${context.getString(R.string.notif_hours)} 47 ${context.getString(R.string.notif_minutes)}"
                                com.focusapp.notification.NotificationHelper.showNotification(
                                    context,
                                    context.getString(R.string.notif_weekly_title),
                                    String.format(context.getString(R.string.notif_weekly_body), timeText, "🎉"),
                                    1002
                                )
                                // Weekly 5h+ notification
                                val timeText2 = "5 ${context.getString(R.string.notif_hours)} 46 ${context.getString(R.string.notif_minutes)}"
                                com.focusapp.notification.NotificationHelper.showNotification(
                                    context,
                                    context.getString(R.string.notif_weekly_title),
                                    String.format(context.getString(R.string.notif_weekly_body), timeText2, "🔥"),
                                    1003
                                )
                                // Weekly 10h+ notification
                                com.focusapp.notification.NotificationHelper.showNotification(
                                    context,
                                    context.getString(R.string.notif_weekly_title),
                                    context.getString(R.string.notif_weekly_10h),
                                    1004
                                )
                            },
                            textColor = Color(0xFFFF9800)
                        )
                    }
                }
            }
        }
    }
}

// Helper function to get language display name
private fun getLanguageDisplayName(languageCode: String): String {
    return when (languageCode) {
        "en" -> "English"
        "fr" -> "Français"
        "tr" -> "Türkçe"
        "it" -> "Italiano"
        "de" -> "Deutsch"
        "es" -> "Español"
        else -> "English"
    }
}

@Composable
private fun ClickableSettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: Color
) {
    val scale = LocalScreenScale.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = GeistFontFamily,
                    fontSize = 15.sp.scaled(scale, min = 12.sp),
                    fontWeight = FontWeight.Normal,
                    color = textColor
                )
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontFamily = GeistFontFamily,
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.6f)
                    )
                )
            }
        }
        
        // Chevron arrow
        Canvas(modifier = Modifier.size(16.dp)) {
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
private fun SwitchSettingItem(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textColor: Color
) {
    val switchScale = LocalScreenScale.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = GeistFontFamily,
                fontSize = 15.sp.scaled(switchScale, min = 12.sp),
                fontWeight = FontWeight.Normal,
                color = textColor
            )
        )
        
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF4CAF50),
                checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
            )
        )
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
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.theme),  // Use string resource
            style = TextStyle(
                fontFamily = GeistFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = textColor
            )
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isDark) stringResource(R.string.dark) else stringResource(R.string.light),
                style = TextStyle(
                    fontFamily = GeistFontFamily,
                    fontSize = 12.sp,
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
    // Get current time for preview
    val calendar = remember { java.util.Calendar.getInstance() }
    var currentTimePreview by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        while (true) {
            val cal = java.util.Calendar.getInstance()
            val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = cal.get(java.util.Calendar.MINUTE)
            currentTimePreview = String.format("%02d:%02d", hour, minute)
            kotlinx.coroutines.delay(1000)
        }
    }
    
    // Font key to FontFamily mapping
    val fontFamilyMap = mapOf(
        "menil" to MenilFontFamily,
        "avocado" to com.focusapp.ui.theme.AvocadoFontFamily,
        "break" to com.focusapp.ui.theme.BreakFontFamily,
        "dxburst" to com.focusapp.ui.theme.DxburstFontFamily,
        "kiya" to com.focusapp.ui.theme.KiyaFontFamily,
        "flaviotte" to com.focusapp.ui.theme.FlaviotteFontFamily,
        "awesome" to com.focusapp.ui.theme.AwesomeWaysFontFamily,
        "tehegan" to com.focusapp.ui.theme.TeheganFontFamily,
        "wonderia" to com.focusapp.ui.theme.WonderiaFontFamily,
        "kino40" to com.focusapp.ui.theme.Kino40FontFamily,
        "1797" to com.focusapp.ui.theme.Font1797FontFamily,
        "glina" to com.focusapp.ui.theme.GlinaFontFamily,
        "sentient" to com.focusapp.ui.theme.SentientFontFamily,
        "chillax" to com.focusapp.ui.theme.ChillaxFontFamily
    )
    
    val fontOptions = listOf(
        "Menil-Étroit" to "menil",
        "LT Avocado" to "avocado",
        "Break" to "break",
        "DXBurst Smooth" to "dxburst",
        "Kiya Handwrite" to "kiya",
        "Flaviotte" to "flaviotte",
        "Awesome Ways" to "awesome",
        "Tehegan" to "tehegan",
        "Wonderia" to "wonderia",
        "Kino 40" to "kino40",
        "1797 Medium" to "1797",
        "Glina Script" to "glina",
        "Sentient" to "sentient",
        "Chillax" to "chillax"
    )
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        
        // Font options with live time preview
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            fontOptions.forEachIndexed { index, (_, value) ->
                val isSelected = selectedFont == value
                val fontFamily = fontFamilyMap[value] ?: MenilFontFamily
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            role = Role.RadioButton,
                            onClick = { onFontSelect(value) }
                        )
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Time preview in the actual font
                    Text(
                        text = currentTimePreview,
                        style = TextStyle(
                            fontFamily = fontFamily,
                            fontSize = 32.sp.scaled(LocalScreenScale.current, min = 20.sp),
                            fontWeight = FontWeight.Normal,
                            color = textColor.copy(alpha = if (isSelected) 1f else 0.5f),
                            letterSpacing = 1.sp
                        )
                    )
                    
                    if (isSelected) {
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
                
                // Add divider between items
                if (index < fontOptions.size - 1) {
                    Divider(
                        color = textColor.copy(alpha = 0.1f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageSubmenu(
    selectedLanguage: String,
    onLanguageSelect: (String) -> Unit,
    onBack: () -> Unit,
    textColor: Color
) {
    Column(
        modifier = Modifier.fillMaxSize(),  // Removed verticalScroll - parent is already scrollable
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Back button

        
        // Language options
        val languageOptions = listOf(
            "English" to "en",
            "Français" to "fr",
            "Türkçe" to "tr",
            "Italiano" to "it",
            "Deutsch" to "de",
            "Español" to "es"
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            languageOptions.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            role = Role.RadioButton,
                            onClick = { onLanguageSelect(value) }
                        )
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = TextStyle(
                            fontFamily = GeistFontFamily,
                            fontSize = 16.sp,
                            color = textColor.copy(alpha = if (selectedLanguage == value) 1f else 0.6f),
                            fontWeight = if (selectedLanguage == value) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                    
                    if (selectedLanguage == value) {
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
private fun BreakSubmenu(
    autoBreakEnabled: Boolean,
    breakDurationMinutes: Int,
    onAutoBreakChange: (Boolean) -> Unit,
    onDurationChange: (Int) -> Unit,
    onBack: () -> Unit,
    textColor: Color
) {
    var showCustomInput by remember { mutableStateOf(false) }
    var customMinutes by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        
        // Break Duration section (always visible)
        Text(
            text = stringResource(R.string.break_duration),
            style = TextStyle(
                fontFamily = GeistFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = textColor
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
        
        // Duration options
        val durationOptions = listOf(5, 10, 15, 20, 30)
        
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            durationOptions.forEach { minutes ->
                val isSelected = breakDurationMinutes == minutes && !showCustomInput
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            role = Role.RadioButton,
                            onClick = {
                                showCustomInput = false
                                onDurationChange(minutes)
                            }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$minutes ${stringResource(R.string.break_min_suffix)}",
                        style = TextStyle(
                            fontFamily = GeistFontFamily,
                            fontSize = 16.sp,
                            color = textColor.copy(alpha = if (isSelected) 1f else 0.6f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                    
                    if (isSelected) {
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
            
            // Custom option
            val isCustomSelected = showCustomInput || !durationOptions.contains(breakDurationMinutes)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        role = Role.RadioButton,
                        onClick = {
                            showCustomInput = true
                            customMinutes = if (!durationOptions.contains(breakDurationMinutes)) {
                                breakDurationMinutes.toString()
                            } else ""
                        }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.break_custom),
                    style = TextStyle(
                        fontFamily = GeistFontFamily,
                        fontSize = 16.sp,
                        color = textColor.copy(alpha = if (isCustomSelected) 1f else 0.6f),
                        fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.Normal
                    )
                )
                
                if (isCustomSelected) {
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
            
            // Custom input field
            if (showCustomInput || !durationOptions.contains(breakDurationMinutes)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                                customMinutes = newValue
                                newValue.toIntOrNull()?.let { mins ->
                                    if (mins in 1..999) {
                                        onDurationChange(mins)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.width(100.dp),
                        textStyle = TextStyle(
                            fontFamily = GeistFontFamily,
                            fontSize = 16.sp,
                            color = textColor
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = textColor.copy(alpha = 0.3f),
                            cursorColor = Color(0xFF4CAF50)
                        )
                    )
                    
                    Text(
                        text = stringResource(R.string.break_min_suffix),
                        style = TextStyle(
                            fontFamily = GeistFontFamily,
                            fontSize = 16.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
        
        Divider(color = textColor.copy(alpha = 0.1f))
        
        // Auto break toggle (at the bottom)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.auto_break_start),
                    style = TextStyle(
                        fontFamily = GeistFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = textColor
                    )
                )
                
                Text(
                    text = stringResource(R.string.auto_break_description),
                    style = TextStyle(
                        fontFamily = GeistFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = textColor.copy(alpha = 0.6f),
                        lineHeight = 18.sp
                    )
                )
            }
            
            Switch(
                checked = autoBreakEnabled,
                onCheckedChange = onAutoBreakChange,
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
                fontFamily = GeistFontFamily,
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
                            fontFamily = GeistFontFamily,
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

@Composable
private fun AboutSubmenu(
    onBack: () -> Unit,
    textColor: Color,
    isDark: Boolean
) {
    val accentColor = Color(0xFF4CAF50)
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back button header
        Spacer(modifier = Modifier.height(8.dp))
        
        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App logo
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = "Clockera Logo",
                modifier = Modifier
                    .size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // App name
            Text(
                text = "Clockera",
                style = TextStyle(
                    fontFamily = GeistFontFamily,
                    fontSize = 28.sp.scaled(LocalScreenScale.current, min = 18.sp),
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    letterSpacing = 1.sp
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subtle tagline
            Text(
                text = stringResource(R.string.about_tagline),
                style = TextStyle(
                    fontFamily = GeistFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = accentColor,
                    letterSpacing = 0.5.sp
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(1.dp)
                    .background(textColor.copy(alpha = 0.1f))
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Mission text
            Text(
                text = stringResource(R.string.about_description),
                style = TextStyle(
                    fontFamily = GeistFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = textColor.copy(alpha = 0.75f),
                    lineHeight = 24.sp,
                    letterSpacing = 0.2.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Copyright
            Text(
                text = stringResource(R.string.copyright),
                style = TextStyle(
                    fontFamily = GeistFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = textColor.copy(alpha = 0.35f),
                    letterSpacing = 0.3.sp
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
private fun RewardsSubmenu(
    textColor: Color,
    isDark: Boolean
) {
    val cardBg = if (isDark) Color(0xFF2A2E24) else Color(0xFFF0F0F0)
    val lockColor = textColor.copy(alpha = 0.3f)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // 5 rows x 3 columns grid
        for (row in 0 until 5) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (col in 0 until 3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(cardBg, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Lock icon using Canvas
                        Canvas(modifier = Modifier.size(28.dp)) {
                            val w = size.width
                            val h = size.height
                            val strokeW = 2.dp.toPx()

                            // Lock body (rectangle)
                            val bodyTop = h * 0.45f
                            val bodyLeft = w * 0.2f
                            val bodyRight = w * 0.8f
                            val bodyBottom = h * 0.9f
                            drawRoundRect(
                                color = lockColor,
                                topLeft = Offset(bodyLeft, bodyTop),
                                size = androidx.compose.ui.geometry.Size(bodyRight - bodyLeft, bodyBottom - bodyTop),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                                style = Stroke(width = strokeW)
                            )

                            // Lock shackle (arc)
                            val shackleLeft = w * 0.3f
                            val shackleRight = w * 0.7f
                            val shackleTop = h * 0.12f
                            drawArc(
                                color = lockColor,
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = Offset(shackleLeft, shackleTop),
                                size = androidx.compose.ui.geometry.Size(shackleRight - shackleLeft, (bodyTop - shackleTop) * 2),
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )

                            // Keyhole dot
                            drawCircle(
                                color = lockColor,
                                radius = 2.5.dp.toPx(),
                                center = Offset(w / 2, h * 0.62f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionsSubmenu(
    currentSessions: Int,
    onSessionsChange: (Int) -> Unit,
    textColor: Color
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        for (count in 2..10) {
            val isSelected = currentSessions == count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        role = Role.RadioButton,
                        onClick = { onSessionsChange(count) }
                    )
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format(stringResource(R.string.sessions_count), count),
                    style = TextStyle(
                        fontFamily = GeistFontFamily,
                        fontSize = 16.sp,
                        color = textColor.copy(alpha = if (isSelected) 1f else 0.6f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                )

                if (isSelected) {
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

            if (count < 10) {
                Divider(
                    color = textColor.copy(alpha = 0.1f),
                    thickness = 0.5.dp
                )
            }
        }
    }
}


// Helper function to update locale
private fun updateLocale(context: android.content.Context, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}

