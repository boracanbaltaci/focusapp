package com.focusapp.data.model

import com.google.gson.annotations.SerializedName

data class SessionResponse(
    val id: Long,
    val startTime: String,
    val endTime: String?,
    val durationSeconds: Long?,
    val isBreak: Boolean
)

data class WeeklyStatsResponse(
    val dailyDurations: Map<String, Long>
)

data class HourlyStatsResponse(
    val hourlyDurations: Map<Int, Long>
)

data class User(
    val username: String,
    val language: String = "en",
    val clockType: String = "digital",
    val style: String = "default",
    val background: String = "default"
)

// API Request/Response models for backend communication

data class FocusSessionRequest(
    @SerializedName("startTime")
    val startTime: String,
    
    @SerializedName("isBreak")
    val isBreak: Boolean = false,
    
    @SerializedName("category")
    val category: String? = null,
    
    @SerializedName("notes")
    val notes: String? = null
)

data class FocusSessionResponse(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("userId")
    val userId: Long? = null,
    
    @SerializedName("startTime")
    val startTime: String,
    
    @SerializedName("endTime")
    val endTime: String? = null,
    
    @SerializedName("durationSeconds")
    val durationSeconds: Long? = null,
    
    @SerializedName("isBreak")
    val isBreak: Boolean = false,
    
    @SerializedName("category")
    val category: String? = null,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("completed")
    val completed: Boolean = false
)
