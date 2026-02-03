package com.focusapp.data.network

import com.google.gson.annotations.SerializedName

/**
 * Request model for creating or updating a focus session
 */
data class FocusSessionRequest(
    @SerializedName("startTime")
    val startTime: String,
    
    @SerializedName("endTime")
    val endTime: String? = null,
    
    @SerializedName("durationSeconds")
    val durationSeconds: Long? = null,
    
    @SerializedName("isBreak")
    val isBreak: Boolean = false
)

/**
 * Response model for a focus session from the API
 */
data class FocusSessionResponse(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("startTime")
    val startTime: String,
    
    @SerializedName("endTime")
    val endTime: String? = null,
    
    @SerializedName("durationSeconds")
    val durationSeconds: Long? = null,
    
    @SerializedName("isBreak")
    val isBreak: Boolean = false
)
