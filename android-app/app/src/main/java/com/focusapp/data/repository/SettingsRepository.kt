package com.focusapp.data.repository

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("focus_app_settings", Context.MODE_PRIVATE)
    
    fun getClockType(): String {
        return prefs.getString("clock_type", "digital") ?: "digital"
    }
    
    fun setClockType(clockType: String) {
        prefs.edit().putString("clock_type", clockType).apply()
    }
    
    fun getStyle(): String {
        return prefs.getString("style", "default") ?: "default"
    }
    
    fun setStyle(style: String) {
        prefs.edit().putString("style", style).apply()
    }
    
    fun getBackground(): String {
        return prefs.getString("background", "default") ?: "default"
    }
    
    fun setBackground(background: String) {
        prefs.edit().putString("background", background).apply()
    }
    
    fun getLanguage(): String {
        return prefs.getString("language", "en") ?: "en"
    }
    
    fun setLanguage(language: String) {
        prefs.edit().putString("language", language).apply()
    }
}
