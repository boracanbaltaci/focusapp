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
    
    fun saveSession(durationMinutes: Int, elapsedSeconds: Int = 0) {
        val sessions = getAllSessions().toMutableList()
        // Save at least 1 minute if any time was spent
        val effectiveMinutes = if (durationMinutes == 0 && elapsedSeconds > 0) 1 else durationMinutes.coerceAtLeast(if (elapsedSeconds > 0) 1 else 0)
        sessions.add(SessionData(System.currentTimeMillis(), effectiveMinutes))
        
        val json = gson.toJson(sessions)
        prefs.edit().putString("sessions", json).apply()
    }
    
    fun getAllSessions(): List<SessionData> {
        val json = prefs.getString("sessions", null) ?: return emptyList()
        val type = object : TypeToken<List<SessionData>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun getWeeklyData(): Map<Int, Int> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val weekStart = calendar.timeInMillis
        
        val sessions = getAllSessions().filter { it.date >= weekStart }
        val weekData = mutableMapOf<Int, Int>()
        
        // Initialize all 7 days with 0
        for (i in 1..7) {
            weekData[i] = 0
        }
        
        val sessionCal = Calendar.getInstance()
        sessions.forEach { session ->
            sessionCal.timeInMillis = session.date
            val dayOfWeek = sessionCal.get(Calendar.DAY_OF_WEEK)
            // Convert Sunday=1 to Monday=1 system
            val adjustedDay = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
            weekData[adjustedDay] = (weekData[adjustedDay] ?: 0) + session.durationMinutes
        }
        
        return weekData
    }
    
    fun getMonthlyData(): Map<Int, Int> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        // Pre-compute daysInMonth before any mutation
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val filterCal = Calendar.getInstance()
        val sessions = getAllSessions().filter { session ->
            filterCal.timeInMillis = session.date
            filterCal.get(Calendar.MONTH) == currentMonth && 
            filterCal.get(Calendar.YEAR) == currentYear
        }
        
        val monthData = mutableMapOf<Int, Int>()
        
        // Initialize all days with 0
        for (i in 1..daysInMonth) {
            monthData[i] = 0
        }
        
        val sessionCal = Calendar.getInstance()
        sessions.forEach { session ->
            sessionCal.timeInMillis = session.date
            val day = sessionCal.get(Calendar.DAY_OF_MONTH)
            monthData[day] = (monthData[day] ?: 0) + session.durationMinutes
        }
        
        return monthData
    }
    
    fun getYearlyData(): Map<Int, Int> {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        
        val filterCal = Calendar.getInstance()
        val sessions = getAllSessions().filter { session ->
            filterCal.timeInMillis = session.date
            filterCal.get(Calendar.YEAR) == currentYear
        }
        
        val yearData = mutableMapOf<Int, Int>()
        
        // Initialize all 12 months with 0
        for (i in 1..12) {
            yearData[i] = 0
        }
        
        val sessionCal = Calendar.getInstance()
        sessions.forEach { session ->
            sessionCal.timeInMillis = session.date
            val month = sessionCal.get(Calendar.MONTH) + 1 // 0-based to 1-based
            yearData[month] = (yearData[month] ?: 0) + session.durationMinutes
        }
        
        return yearData
    }
    
    fun getTotalMinutes(data: Map<Int, Int>): Int {
        return data.values.sum()
    }
}
