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
    
    fun saveSession(durationMinutes: Int) {
        val sessions = getAllSessions().toMutableList()
        sessions.add(SessionData(System.currentTimeMillis(), durationMinutes))
        
        val json = gson.toJson(sessions)
        prefs.edit().putString("sessions", json).apply()
    }
    
    fun getAllSessions(): List<SessionData> {
        val json = prefs.getString("sessions", null) ?: return emptyList()
        val type = object : TypeToken<List<SessionData>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun getDailyData(): Map<Int, Int> {
        val calendar = Calendar.getInstance()
        
        // Set calendar to start of today
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val dayStart = calendar.timeInMillis
        
        // Set calendar to end of today
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val dayEnd = calendar.timeInMillis
        
        val sessions = getAllSessions().filter { it.date >= dayStart && it.date <= dayEnd }
        val hourlyData = mutableMapOf<Int, Int>()
        
        // Initialize all 24 hours with 0
        for (i in 0..23) {
            hourlyData[i] = 0
        }
        
        sessions.forEach { session ->
            calendar.timeInMillis = session.date
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            hourlyData[hour] = hourlyData[hour]!! + session.durationMinutes
        }
        
        return hourlyData
    }
    
    fun getWeeklyData(): Map<Int, Int> {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val weekStart = calendar.timeInMillis
        
        val sessions = getAllSessions().filter { it.date >= weekStart }
        val weekData = mutableMapOf<Int, Int>()
        
        // Initialize all 7 days with 0
        for (i in 1..7) {
            weekData[i] = 0
        }
        
        sessions.forEach { session ->
            calendar.timeInMillis = session.date
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            // Convert Sunday=1 to Monday=1 system
            val adjustedDay = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
            weekData[adjustedDay] = weekData[adjustedDay]!! + session.durationMinutes
        }
        
        return weekData
    }
    
    fun getMonthlyData(): Map<Int, Int> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val monthStart = calendar.timeInMillis
        
        val sessions = getAllSessions().filter { session ->
            calendar.timeInMillis = session.date
            calendar.get(Calendar.MONTH) == currentMonth && 
            calendar.get(Calendar.YEAR) == currentYear
        }
        
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val monthData = mutableMapOf<Int, Int>()
        
        // Initialize all days with 0
        for (i in 1..daysInMonth) {
            monthData[i] = 0
        }
        
        sessions.forEach { session ->
            calendar.timeInMillis = session.date
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            monthData[day] = monthData[day]!! + session.durationMinutes
        }
        
        return monthData
    }
    
    fun getYearlyData(): Map<Int, Int> {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        
        val sessions = getAllSessions().filter { session ->
            calendar.timeInMillis = session.date
            calendar.get(Calendar.YEAR) == currentYear
        }
        
        val yearData = mutableMapOf<Int, Int>()
        
        // Initialize all 12 months with 0
        for (i in 1..12) {
            yearData[i] = 0
        }
        
        sessions.forEach { session ->
            calendar.timeInMillis = session.date
            val month = calendar.get(Calendar.MONTH) + 1 // 0-based to 1-based
            yearData[month] = yearData[month]!! + session.durationMinutes
        }
        
        return yearData
    }
    
    fun getTotalMinutes(data: Map<Int, Int>): Int {
        return data.values.sum()
    }
}
