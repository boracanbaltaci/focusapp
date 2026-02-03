package com.focusapp.data.repository

import android.content.Context
import android.util.Log
import com.focusapp.data.local.AppDatabase
import com.focusapp.data.local.SessionEntity
import com.focusapp.data.model.*
import com.focusapp.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SessionRepository(context: Context) {
    
    private val sessionDao = AppDatabase.getDatabase(context).sessionDao()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val apiService = RetrofitClient.apiService
    
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
            val id = sessionDao.insertSession(session)
            Result.success(SessionResponse(
                id = id,
                startTime = dateFormat.format(Date(currentTime)),
                endTime = null,
                durationSeconds = null,
                isBreak = isBreak
            ))
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
                sessionDao.updateSession(updatedSession)
                
                val sessionResponse = SessionResponse(
                    id = session.id,
                    startTime = dateFormat.format(Date(session.startTime)),
                    endTime = dateFormat.format(Date(endTime)),
                    durationSeconds = durationSeconds,
                    isBreak = session.isBreak
                )
                
                // Send to backend asynchronously (don't block on failure)
                sendSessionToBackend(sessionResponse)
                
                Result.success(sessionResponse)
            } else {
                Result.failure(Exception("Session not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send completed session data to backend.
     * This is called asynchronously and failures are logged but don't affect the local operation.
     */
    private suspend fun sendSessionToBackend(session: SessionResponse) {
        withContext(Dispatchers.IO) {
            try {
                // Create the session on backend
                val request = FocusSessionRequest(
                    startTime = session.startTime,
                    endTime = session.endTime,
                    durationSeconds = session.durationSeconds,
                    isBreak = session.isBreak
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
