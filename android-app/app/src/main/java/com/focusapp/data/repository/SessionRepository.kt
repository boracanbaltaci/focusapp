package com.focusapp.data.repository

import com.focusapp.data.api.RetrofitClient
import com.focusapp.data.model.*

class SessionRepository {
    
    private val api = RetrofitClient.apiService
    
    suspend fun startSession(isBreak: Boolean = false): Result<SessionResponse> {
        return try {
            val response = api.startSession(SessionRequest(isBreak))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to start session"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun endSession(sessionId: Long): Result<SessionResponse> {
        return try {
            val response = api.endSession(sessionId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to end session"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getWeeklyStats(): Result<WeeklyStatsResponse> {
        return try {
            val response = api.getWeeklyStats()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch stats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getHourlyStats(): Result<HourlyStatsResponse> {
        return try {
            val response = api.getHourlyStats()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Failed to fetch stats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
