package com.focusapp.data.network

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages authentication token storage and retrieval.
 * Uses SharedPreferences for simple token storage.
 */
class TokenManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    /**
     * Save authentication token.
     */
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }
    
    /**
     * Retrieve authentication token.
     * Returns null if no token is stored.
     */
    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }
    
    /**
     * Clear stored authentication token.
     */
    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }
    
    /**
     * Check if a token is stored.
     */
    fun hasToken(): Boolean {
        return getToken() != null
    }
    
    companion object {
        private const val PREFS_NAME = "focus_app_prefs"
        private const val KEY_TOKEN = "auth_token"
    }
}
