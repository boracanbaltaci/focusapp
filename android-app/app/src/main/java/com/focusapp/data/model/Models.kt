package com.focusapp.data.model

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
    val startTime: String,
    val endTime: String?,
    val durationSeconds: Long?,
    val isBreak: Boolean
)

data class FocusSessionResponse(
    val id: Long,
    val startTime: String,
    val endTime: String?,
    val durationSeconds: Long?,
    val isBreak: Boolean,
    val userId: Long? = null
)
