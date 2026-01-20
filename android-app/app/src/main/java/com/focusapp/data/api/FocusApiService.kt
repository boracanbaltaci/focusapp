package com.focusapp.data.api

import com.focusapp.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface FocusApiService {
    
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("api/sessions/start")
    suspend fun startSession(@Body request: SessionRequest): Response<SessionResponse>
    
    @PUT("api/sessions/{id}/end")
    suspend fun endSession(@Path("id") id: Long): Response<SessionResponse>
    
    @GET("api/sessions/stats/weekly")
    suspend fun getWeeklyStats(): Response<WeeklyStatsResponse>
    
    @GET("api/sessions/stats/hourly")
    suspend fun getHourlyStats(): Response<HourlyStatsResponse>
}
