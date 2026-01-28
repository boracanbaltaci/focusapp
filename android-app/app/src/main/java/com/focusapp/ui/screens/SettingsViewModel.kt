package com.focusapp.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import com.focusapp.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(context: Context) : ViewModel() {
    
    private val settingsRepository = SettingsRepository(context)
    
    private val _clockType = MutableStateFlow(settingsRepository.getClockType())
    val clockType: StateFlow<String> = _clockType
    
    private val _style = MutableStateFlow(settingsRepository.getStyle())
    val style: StateFlow<String> = _style
    
    private val _background = MutableStateFlow(settingsRepository.getBackground())
    val background: StateFlow<String> = _background
    
    private val _language = MutableStateFlow(settingsRepository.getLanguage())
    val language: StateFlow<String> = _language
    
    fun setClockType(clockType: String) {
        _clockType.value = clockType
        settingsRepository.setClockType(clockType)
    }
    
    fun setStyle(style: String) {
        _style.value = style
        settingsRepository.setStyle(style)
    }
    
    fun setBackground(background: String) {
        _background.value = background
        settingsRepository.setBackground(background)
    }
    
    fun setLanguage(language: String) {
        _language.value = language
        settingsRepository.setLanguage(language)
    }
}
