package com.focusapp.data.network

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
     */
    @POST("/api/sessions")
    suspend fun createSession(
        @Body request: FocusSessionRequest
    ): Response<FocusSessionResponse>
    
    /**
     * Mark a session as completed.
     * POST /api/sessions/{id}/complete
     */
    @POST("/api/sessions/{id}/complete")
    suspend fun completeSession(
        @Path("id") sessionId: Long
    ): Response<FocusSessionResponse>
    
    /**
     * Update an existing session.
     * PUT /api/sessions/{id}
     */
    @PUT("/api/sessions/{id}")
    suspend fun updateSession(
        @Path("id") sessionId: Long,
        @Body request: FocusSessionRequest
    ): Response<FocusSessionResponse>
}
