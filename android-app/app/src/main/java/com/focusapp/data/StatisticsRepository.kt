package com.focusapp.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class StatisticsRepository(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("statistics_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    fun saveSession(durationMinutes: Int, elapsedSeconds: Int = 0, category: String? = null, isBreak: Boolean = false) {
        val sessions = getAllSessions().toMutableList()
        // Save at least 1 minute if any time was spent
        val effectiveMinutes = if (durationMinutes == 0 && elapsedSeconds > 0) 1 else durationMinutes.coerceAtLeast(if (elapsedSeconds > 0) 1 else 0)
        sessions.add(SessionData(System.currentTimeMillis(), effectiveMinutes, category, isBreak))
        
        val json = gson.toJson(sessions)
        prefs.edit().putString("sessions", json).apply()
    }
    
    fun getAllSessions(): List<SessionData> {
        val json = prefs.getString("sessions", null) ?: return emptyList()
        val type = object : TypeToken<List<SessionData>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    data class DayStats(val focusMinutes: Int, val breakMinutes: Int)
    
    fun getWeeklyData(category: String? = null): Map<Int, DayStats> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val weekStart = calendar.timeInMillis
        
        val sessions = getAllSessions().filter { 
            it.date >= weekStart && (category == null || it.category == category)
        }
        val weekData = mutableMapOf<Int, DayStats>()
        
        // Initialize all 7 days
        for (i in 1..7) {
            weekData[i] = DayStats(0, 0)
        }
        
        val sessionCal = Calendar.getInstance()
        sessions.forEach { session ->
            sessionCal.timeInMillis = session.date
            val dayOfWeek = sessionCal.get(Calendar.DAY_OF_WEEK)
            // Convert Sunday=1 to Monday=1 system
            val adjustedDay = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
            
            val current = weekData[adjustedDay] ?: DayStats(0, 0)
            if (session.isBreak) {
                weekData[adjustedDay] = current.copy(breakMinutes = current.breakMinutes + session.durationMinutes)
            } else {
                weekData[adjustedDay] = current.copy(focusMinutes = current.focusMinutes + session.durationMinutes)
            }
        }
        
        return weekData
    }

    fun getPreviousWeeklyData(category: String? = null): Map<Int, DayStats> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val weekStart = calendar.timeInMillis
        val prevWeekStart = weekStart - 7 * 24 * 60 * 60 * 1000L
        
        val sessions = getAllSessions().filter { 
            it.date >= prevWeekStart && it.date < weekStart && (category == null || it.category == category)
        }
        val weekData = mutableMapOf<Int, DayStats>()
        for (i in 1..7) {
            weekData[i] = DayStats(0, 0)
        }
        
        val sessionCal = Calendar.getInstance()
        sessions.forEach { session ->
            sessionCal.timeInMillis = session.date
            val dayOfWeek = sessionCal.get(Calendar.DAY_OF_WEEK)
            val adjustedDay = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
            
            val current = weekData[adjustedDay] ?: DayStats(0, 0)
            if (session.isBreak) {
                weekData[adjustedDay] = current.copy(breakMinutes = current.breakMinutes + session.durationMinutes)
            } else {
                weekData[adjustedDay] = current.copy(focusMinutes = current.focusMinutes + session.durationMinutes)
            }
        }
        return weekData
    }
    
    fun getMonthlyData(category: String? = null): Map<Int, DayStats> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        // Pre-compute daysInMonth before any mutation
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val filterCal = Calendar.getInstance()
        val sessions = getAllSessions().filter { session ->
            filterCal.timeInMillis = session.date
            filterCal.get(Calendar.MONTH) == currentMonth && 
            filterCal.get(Calendar.YEAR) == currentYear &&
            (category == null || session.category == category)
        }
        
        val monthData = mutableMapOf<Int, DayStats>()
        
        // Initialize all days
        for (i in 1..daysInMonth) {
            monthData[i] = DayStats(0, 0)
        }
        
        val sessionCal = Calendar.getInstance()
        sessions.forEach { session ->
            sessionCal.timeInMillis = session.date
            val day = sessionCal.get(Calendar.DAY_OF_MONTH)
            val current = monthData[day] ?: DayStats(0, 0)
            if (session.isBreak) {
                monthData[day] = current.copy(breakMinutes = current.breakMinutes + session.durationMinutes)
            } else {
                monthData[day] = current.copy(focusMinutes = current.focusMinutes + session.durationMinutes)
            }
        }
        
        return monthData
    }

    fun getPreviousMonthlyData(category: String? = null): Map<Int, DayStats> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val prevMonth = calendar.get(Calendar.MONTH)
        val prevYear = calendar.get(Calendar.YEAR)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val filterCal = Calendar.getInstance()
        val sessions = getAllSessions().filter { session ->
            filterCal.timeInMillis = session.date
            filterCal.get(Calendar.MONTH) == prevMonth && 
            filterCal.get(Calendar.YEAR) == prevYear &&
            (category == null || session.category == category)
        }
        
        val monthData = mutableMapOf<Int, DayStats>()
        for (i in 1..daysInMonth) {
            monthData[i] = DayStats(0, 0)
        }
        
        val sessionCal = Calendar.getInstance()
        sessions.forEach { session ->
            sessionCal.timeInMillis = session.date
            val day = sessionCal.get(Calendar.DAY_OF_MONTH)
            val current = monthData[day] ?: DayStats(0, 0)
            if (session.isBreak) {
                monthData[day] = current.copy(breakMinutes = current.breakMinutes + session.durationMinutes)
            } else {
                monthData[day] = current.copy(focusMinutes = current.focusMinutes + session.durationMinutes)
            }
        }
        return monthData
    }
    
    fun getYearlyData(category: String? = null): Map<Int, DayStats> {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        
        val filterCal = Calendar.getInstance()
        val sessions = getAllSessions().filter { session ->
            filterCal.timeInMillis = session.date
            filterCal.get(Calendar.YEAR) == currentYear &&
            (category == null || session.category == category)
        }
        
        val yearData = mutableMapOf<Int, DayStats>()
        
        // Initialize all 12 months
        for (i in 1..12) {
            yearData[i] = DayStats(0, 0)
        }
        
        val sessionCal = Calendar.getInstance()
        sessions.forEach { session ->
            sessionCal.timeInMillis = session.date
            val month = sessionCal.get(Calendar.MONTH) + 1 // 0-based to 1-based
            val current = yearData[month] ?: DayStats(0, 0)
            if (session.isBreak) {
                yearData[month] = current.copy(breakMinutes = current.breakMinutes + session.durationMinutes)
            } else {
                yearData[month] = current.copy(focusMinutes = current.focusMinutes + session.durationMinutes)
            }
        }
        
        return yearData
    }

    fun getPreviousYearlyData(category: String? = null): Map<Int, DayStats> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -1)
        val prevYear = calendar.get(Calendar.YEAR)
        
        val filterCal = Calendar.getInstance()
        val sessions = getAllSessions().filter { session ->
            filterCal.timeInMillis = session.date
            filterCal.get(Calendar.YEAR) == prevYear &&
            (category == null || session.category == category)
        }
        
        val yearData = mutableMapOf<Int, DayStats>()
        for (i in 1..12) {
            yearData[i] = DayStats(0, 0)
        }
        
        val sessionCal = Calendar.getInstance()
        sessions.forEach { session ->
            sessionCal.timeInMillis = session.date
            val month = sessionCal.get(Calendar.MONTH) + 1 // 0-based to 1-based
            val current = yearData[month] ?: DayStats(0, 0)
            if (session.isBreak) {
                yearData[month] = current.copy(breakMinutes = current.breakMinutes + session.durationMinutes)
            } else {
                yearData[month] = current.copy(focusMinutes = current.focusMinutes + session.durationMinutes)
            }
        }
        return yearData
    }
    
    fun getTotalMinutes(data: Map<Int, DayStats>): Int {
        return data.values.sumOf { it.focusMinutes }
    }
    
    /**
     * Returns total minutes per category for the current week.
     * Returns a map of category name -> total minutes.
     */
    fun getWeeklyCategoryBreakdown(): Map<String, Int> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val weekStart = calendar.timeInMillis
        
        val sessions = getAllSessions().filter { it.date >= weekStart && it.category != null }
        val breakdown = mutableMapOf<String, Int>()
        
        sessions.forEach { session ->
            val cat = session.category ?: return@forEach
            breakdown[cat] = (breakdown[cat] ?: 0) + session.durationMinutes
        }
        
        return breakdown
    }

    /**
     * Returns the longest streak: maximum number of consecutive days 
     * that have at least one session > 1 minute.
     */
    fun getLongestStreak(): Int {
        val sessions = getAllSessions().filter { it.durationMinutes >= 1 }
        if (sessions.isEmpty()) return 0

        // Collect unique days that have sessions
        val sessionDays = mutableSetOf<Long>()
        val cal = Calendar.getInstance()
        sessions.forEach { session ->
            cal.timeInMillis = session.date
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            sessionDays.add(cal.timeInMillis)
        }

        val sortedDays = sessionDays.sorted()
        if (sortedDays.isEmpty()) return 0

        var maxStreak = 1
        var currentStreak = 1
        
        for (i in 1 until sortedDays.size) {
            val diff = sortedDays[i] - sortedDays[i - 1]
            // approximate 1 day in millis, allowing for daylight savings
            if (diff in (23 * 60 * 60 * 1000L)..(25 * 60 * 60 * 1000L)) {
                currentStreak++
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak
                }
            } else {
                currentStreak = 1
            }
        }

        return maxStreak
    }
    
    /**
     * Returns the daily average focus time, strictly based on days 
     * where the user actually used the app (has focus session > 1 min).
     */
    fun getAverageDailyActiveMinutes(): Int {
        val sessions = getAllSessions().filter { !it.isBreak && it.durationMinutes >= 1 }
        if (sessions.isEmpty()) return 0
        
        val sessionDays = mutableSetOf<Long>()
        val cal = Calendar.getInstance()
        var totalMinutes = 0
        
        sessions.forEach { session ->
            cal.timeInMillis = session.date
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            sessionDays.add(cal.timeInMillis)
            
            totalMinutes += session.durationMinutes
        }
        
        if (sessionDays.isEmpty()) return 0
        return totalMinutes / sessionDays.size
    }
}
