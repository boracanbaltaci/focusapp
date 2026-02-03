package com.focusapp.data.repository

import android.content.Context
import android.util.Log
import com.focusapp.data.local.AppDatabase
import com.focusapp.data.local.SessionEntity
import com.focusapp.data.model.*
import com.focusapp.data.network.FocusSessionRequest
import com.focusapp.data.network.RetrofitClient
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SessionRepository(context: Context) {
    
    private val sessionDao = AppDatabase.getDatabase(context).sessionDao()
    private val apiService = RetrofitClient.apiService
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    
    companion object {
        private const val TAG = "SessionRepository"
    }
    
    suspend fun startSession(isBreak: Boolean = false): Result<SessionResponse> {
        return try {
            val currentTime = System.currentTimeMillis()
            val session = SessionEntity(
                startTime = currentTime,
                isBreak = isBreak
            )
            
            // Save to local database first (for offline support)
            val localId = sessionDao.insertSession(session)
            
            val sessionResponse = SessionResponse(
                id = localId,
                startTime = dateFormat.format(Date(currentTime)),
                endTime = null,
                durationSeconds = null,
                isBreak = isBreak
            )
            
            // Try to send to backend (non-blocking, failure doesn't prevent local save)
            try {
                val request = FocusSessionRequest(
                    startTime = sessionResponse.startTime,
                    endTime = null,
                    durationSeconds = null,
                    isBreak = isBreak
                )
                val response = apiService.createSession(request)
                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "Session created on backend with ID: ${response.body()?.id}")
                } else {
                    Log.w(TAG, "Failed to create session on backend: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Backend not available, session saved locally only: ${e.message}")
            }
            
            Result.success(sessionResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun endSession(sessionId: Long): Result<SessionResponse> {
        return try {
            val session = sessionDao.getSessionById(sessionId)
            if (session != null) {
                val endTime = System.currentTimeMillis()
                val durationSeconds = TimeUnit.MILLISECONDS.toSeconds(endTime - session.startTime)
                val updatedSession = session.copy(
                    endTime = endTime,
                    durationSeconds = durationSeconds
                )
                
                // Update local database first
                sessionDao.updateSession(updatedSession)
                
                val sessionResponse = SessionResponse(
                    id = session.id,
                    startTime = dateFormat.format(Date(session.startTime)),
                    endTime = dateFormat.format(Date(endTime)),
                    durationSeconds = durationSeconds,
                    isBreak = session.isBreak
                )
                
                // Try to send completion to backend
                try {
                    val response = apiService.completeSession(sessionId)
                    if (response.isSuccessful && response.body() != null) {
                        Log.d(TAG, "Session completed on backend: ${response.body()?.id}")
                    } else {
                        Log.w(TAG, "Failed to complete session on backend: ${response.code()} ${response.message()}")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Backend not available, session completed locally only: ${e.message}")
                }
                
                Result.success(sessionResponse)
            } else {
                Result.failure(Exception("Session not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getWeeklyStats(): Result<WeeklyStatsResponse> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfWeek = calendar.timeInMillis
            
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            val endOfWeek = calendar.timeInMillis
            
            val sessions = sessionDao.getSessionsInRange(startOfWeek, endOfWeek)
            val dailyDurations = mutableMapOf<String, Long>()
            
            val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sessions.filter { it.endTime != null && !it.isBreak }.forEach { session ->
                val day = dayFormat.format(Date(session.startTime))
                dailyDurations[day] = (dailyDurations[day] ?: 0) + (session.durationSeconds ?: 0)
            }
            
            Result.success(WeeklyStatsResponse(dailyDurations))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getHourlyStats(): Result<HourlyStatsResponse> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val endOfDay = calendar.timeInMillis
            
            val sessions = sessionDao.getSessionsInRange(startOfDay, endOfDay)
            val hourlyDurations = mutableMapOf<Int, Long>()
            
            sessions.filter { it.endTime != null && !it.isBreak }.forEach { session ->
                val hour = Calendar.getInstance().apply {
                    timeInMillis = session.startTime
                }.get(Calendar.HOUR_OF_DAY)
                hourlyDurations[hour] = (hourlyDurations[hour] ?: 0) + (session.durationSeconds ?: 0)
            }
            
            Result.success(HourlyStatsResponse(hourlyDurations))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
