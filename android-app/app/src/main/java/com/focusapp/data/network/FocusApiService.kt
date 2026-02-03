package com.focusapp.data.network

import com.focusapp.data.model.FocusSessionRequest
import com.focusapp.data.model.FocusSessionResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service interface for focus session backend endpoints.
 * 
 * Supports:
 * - POST /api/sessions - Create a new focus session
 * - POST /api/sessions/{id}/complete - Mark a session as completed
 * - PUT /api/sessions/{id} - Update a session
 */
interface FocusApiService {
    
    /**
     * Create a new focus session
     * @param request FocusSessionRequest containing session details
     * @return FocusSessionResponse with created session including backend ID
     */
    @POST("/api/sessions")
    suspend fun createSession(
        @Body request: FocusSessionRequest
    ): Response<FocusSessionResponse>
    
    /**
     * Mark a session as completed
     * @param id Session ID from backend
     * @return FocusSessionResponse with updated session details
     */
    @POST("/api/sessions/{id}/complete")
    suspend fun completeSession(
        @Path("id") id: Long
    ): Response<FocusSessionResponse>
    
    /**
     * Update a session
     * @param id Session ID from backend
     * @param request FocusSessionRequest with updated session details
     * @return FocusSessionResponse with updated session
     */
    @PUT("/api/sessions/{id}")
    suspend fun updateSession(
        @Path("id") id: Long,
        @Body request: FocusSessionRequest
    ): Response<FocusSessionResponse>
}
