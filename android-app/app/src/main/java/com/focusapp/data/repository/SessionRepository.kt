package com.focusapp.data.repository

import android.content.Context
import com.focusapp.data.local.AppDatabase
import com.focusapp.data.local.SessionEntity
import com.focusapp.data.model.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SessionRepository(context: Context) {
    
    private val sessionDao = AppDatabase.getDatabase(context).sessionDao()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    
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
                Result.success(SessionResponse(
                    id = session.id,
                    startTime = dateFormat.format(Date(session.startTime)),
                    endTime = dateFormat.format(Date(endTime)),
                    durationSeconds = durationSeconds,
                    isBreak = session.isBreak
                ))
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
