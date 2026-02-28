package com.focusapp.data

import com.google.gson.annotations.SerializedName

data class SessionData(
    @SerializedName("date") val date: Long, // Timestamp in milliseconds
    @SerializedName("durationMinutes") val durationMinutes: Int, // Duration in minutes
    @SerializedName("category") val category: String? = null // Focus category (e.g. "BOOK", "WORK")
)
