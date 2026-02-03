package com.focusapp.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.focusapp.data.model.FocusSessionRequest
import com.focusapp.data.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StatisticsRepository(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("statistics_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val apiService = RetrofitClient.apiService
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    
    companion object {
        private const val TAG = "StatisticsRepository"
    }
    
    fun saveSession(durationMinutes: Int) {
        val currentTime = System.currentTimeMillis()
        val sessions = getAllSessions().toMutableList()
        sessions.add(SessionData(currentTime, durationMinutes))
        
        val json = gson.toJson(sessions)
        prefs.edit().putString("sessions", json).apply()
        
        // Send to backend asynchronously
        sendSessionToBackend(currentTime, durationMinutes)
    }
    
    /**
     * Send completed session data to backend.
     * This is called asynchronously as a fire-and-forget operation.
     * Failures are logged but don't affect the local operation.
     * 
     * Note: Uses GlobalScope since this repository doesn't have lifecycle awareness
     * and the operation should complete even if the calling context is destroyed.
     */
    private fun sendSessionToBackend(startTime: Long, durationMinutes: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Calculate end time based on duration
                val durationSeconds = durationMinutes * 60L
                val endTime = startTime + (durationSeconds * 1000)
                
                // Create the session on backend
                val request = FocusSessionRequest(
                    startTime = dateFormat.format(Date(startTime)),
                    endTime = dateFormat.format(Date(endTime)),
                    durationSeconds = durationSeconds,
                    isBreak = false
                )
                
                val createResponse = apiService.createSession(request)
                
                if (createResponse.isSuccessful) {
                    val backendSession = createResponse.body()
                    if (backendSession != null) {
                        Log.d(TAG, "Session created on backend with ID: ${backendSession.id}")
                        
                        // Mark as completed on backend
                        val completeResponse = apiService.completeSession(backendSession.id)
                        if (completeResponse.isSuccessful) {
                            Log.d(TAG, "Session ${backendSession.id} marked as completed on backend")
                        } else {
                            Log.w(TAG, "Failed to mark session as completed on backend: ${completeResponse.code()}")
                        }
                    }
                } else {
                    Log.w(TAG, "Failed to create session on backend: ${createResponse.code()}")
                }
            } catch (e: Exception) {
                // Log error but don't propagate - backend sync is best-effort
                Log.e(TAG, "Error sending session to backend", e)
            }
        }
    }
    
    fun getAllSessions(): List<SessionData> {
        val json = prefs.getString("sessions", null) ?: return emptyList()
        val type = object : TypeToken<List<SessionData>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
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
