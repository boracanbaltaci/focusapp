package com.focusapp.data.api

import com.focusapp.data.model.FocusSessionRequest
import com.focusapp.data.model.FocusSessionResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service interface for Focus App backend.
 * 
 * All endpoints require Bearer token authentication via Authorization header.
 */
interface FocusApiService {
    
    /**
     * Create a new focus session.
     * POST /api/sessions
     * 
     * @param request Session creation request with startTime, isBreak, etc.
     * @return FocusSessionResponse with session details including generated ID
     */
    @POST("api/sessions")
    suspend fun createSession(
        @Body request: FocusSessionRequest
    ): Response<FocusSessionResponse>
    
    /**
     * Mark a session as completed.
     * POST /api/sessions/{id}/complete
     * 
     * @param id Session ID to complete
     * @return Updated FocusSessionResponse with endTime and durationSeconds
     */
    @POST("api/sessions/{id}/complete")
    suspend fun completeSession(
        @Path("id") id: Long
    ): Response<FocusSessionResponse>
    
    /**
     * Update an existing session.
     * PUT /api/sessions/{id}
     * 
     * @param id Session ID to update
     * @param request Updated session data
     * @return Updated FocusSessionResponse
     */
    @PUT("api/sessions/{id}")
    suspend fun updateSession(
        @Path("id") id: Long,
        @Body request: FocusSessionRequest
    ): Response<FocusSessionResponse>
}
