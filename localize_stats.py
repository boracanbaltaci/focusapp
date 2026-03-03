import os

filepath = r"c:\projects\focusapp\android-app\app\src\main\java\com\focusapp\ui\screens\StatisticsScreen.kt"

with open(filepath, "r", encoding="utf-8") as f:
    text = f.read()

# We need to completely rewrite the StatisticsScreen Composable.
# Let's find the boundaries of the @Composable fun StatisticsScreen(

start_idx = text.find("@Composable\nfun StatisticsScreen(")
if start_idx == -1:
    print("Could not find start")
else:
    # Find the end of StatisticsScreen
    # It ends before "private fun getDayLabel"
    end_idx = text.find("private fun getDayLabel", start_idx)
    
    if end_idx == -1:
        print("Could not find end")
    else:
        new_composable = """@Composable
fun StatisticsScreen(
    onNavigateToSettings: () -> Unit,
    textColor: Color
) {
    val context = LocalContext.current
    val statisticsRepository = remember { StatisticsRepository(context) }
    var viewMode by remember { mutableStateOf(ViewMode.WEEK) }
    var selectedCategoryFilter by remember { mutableStateOf<FocusCategory?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

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
    
    // String resources
    val hoursStr = stringResource(R.string.stat_hours)
    val minutesStr = stringResource(R.string.stat_minutes)
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 24.dp)
        ) {
            // Provide space for settings icon to avoid overlap
            Spacer(modifier = Modifier.height(26.dp))
            
            // Top section: Stats, Central Card, Dropdown
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left Block
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.statistics_title), 
                        color = textColor, 
                        fontFamily = GeistFontFamily, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Pin Category Selector instead of 5 empty circles
                    PinCategorySelector(
                        selectedCategory = selectedCategoryFilter,
                        onCategorySelected = { selectedCategoryFilter = it },
                        textColor = textColor,
                        isDark = isDark
                    )
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
                            text = "$streak ${stringResource(R.string.stat_week)}", // "days" doesn't have direct translation in current strings.xml so leaving similar? Actually wait, let's keep streak dynamic
                            color = orangeText, 
                            fontFamily = GeistFontFamily, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Streak", // Still needs generic translation if not exist
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
                        Row(verticalAlignment = Alignment.Bottom) {
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
                            Text(hoursStr, color = blackText, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = GeistFontFamily, modifier = Modifier.padding(bottom = 2.dp))
                        
                            Spacer(modifier = Modifier.width(6.dp))

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
                            Text(minutesStr, color = blackText, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = GeistFontFamily, modifier = Modifier.padding(bottom = 2.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.stat_total_focus), 
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
                        .padding(top = 18.dp), // Pushed further down to avoid Settings icon
                    contentAlignment = Alignment.TopEnd
                ) {
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDark) Color.White else Color(0xFFE0E0E0))
                                .clickable { isDropdownExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val modeText = when (viewMode) {
                                ViewMode.WEEK -> stringResource(R.string.stat_week)
                                ViewMode.MONTH -> stringResource(R.string.stat_month)
                                ViewMode.YEAR -> stringResource(R.string.stat_year)
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
                        
                        androidx.compose.material3.DropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(stringResource(R.string.stat_week), fontFamily = GeistFontFamily) },
                                onClick = { viewMode = ViewMode.WEEK; isDropdownExpanded = false }
                            )
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(stringResource(R.string.stat_month), fontFamily = GeistFontFamily) },
                                onClick = { viewMode = ViewMode.MONTH; isDropdownExpanded = false }
                            )
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(stringResource(R.string.stat_year), fontFamily = GeistFontFamily) },
                                onClick = { viewMode = ViewMode.YEAR; isDropdownExpanded = false }
                            )
                        }
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
                        stringResource(R.string.stat_total_focus), 
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
                                        h > 0 && m > 0 -> "${h}${hoursStr}\\n${m}${minutesStr}"
                                        h > 0 -> "${h}${hoursStr}"
                                        m > 0 -> "${m}${minutesStr}"
                                        else -> "0 ${minutesStr}"
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
        
        SettingsIconButton(
            onClick = onNavigateToSettings, 
            iconColor = textColor
        )
    }
}
"""
        
        final_text = text[:start_idx] + new_composable + text[end_idx:]
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(final_text)
        print("Done updating")
