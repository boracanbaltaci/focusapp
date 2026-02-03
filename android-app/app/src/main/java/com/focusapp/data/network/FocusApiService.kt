package com.focusapp.data.network

import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service interface for Focus Session endpoints
 * 
 * All endpoints require Bearer token authentication via Authorization header
 * (automatically handled by AuthInterceptor in RetrofitClient)
 */
interface FocusApiService {
    
    /**
     * Create a new focus session
     * 
     * POST /api/sessions
     * 
     * @param request The session data (startTime, endTime, durationSeconds, isBreak)
     * @return Response containing the created session with server-assigned ID
     */
    @POST("/api/sessions")
    suspend fun createSession(
        @Body request: FocusSessionRequest
    ): Response<FocusSessionResponse>
    
    /**
     * Mark a session as completed
     * 
     * POST /api/sessions/{id}/complete
     * 
     * @param id The session ID to mark as complete
     * @return Response containing the updated session data
     */
    @POST("/api/sessions/{id}/complete")
    suspend fun completeSession(
        @Path("id") id: Long
    ): Response<FocusSessionResponse>
    
    /**
     * Update an existing session
     * 
     * PUT /api/sessions/{id}
     * 
     * @param id The session ID to update
     * @param request The updated session data
     * @return Response containing the updated session data
     */
    @PUT("/api/sessions/{id}")
    suspend fun updateSession(
        @Path("id") id: Long,
        @Body request: FocusSessionRequest
    ): Response<FocusSessionResponse>
}
