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
    
    private val _clockFont = MutableStateFlow(settingsRepository.getClockFont())
    val clockFont: StateFlow<String> = _clockFont
    
    private val _theme = MutableStateFlow(settingsRepository.getTheme())
    val theme: StateFlow<String> = _theme
    
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
    
    fun setClockFont(font: String) {
        _clockFont.value = font
        settingsRepository.setClockFont(font)
    }
    
    fun setTheme(theme: String) {
        _theme.value = theme
        settingsRepository.setTheme(theme)
    }
    
    private val _autoBreakEnabled = MutableStateFlow(settingsRepository.getAutoBreakEnabled())
    val autoBreakEnabled: StateFlow<Boolean> = _autoBreakEnabled
    
    private val _breakDurationMinutes = MutableStateFlow(settingsRepository.getBreakDurationMinutes())
    val breakDurationMinutes: StateFlow<Int> = _breakDurationMinutes
    
    fun setAutoBreakEnabled(enabled: Boolean) {
        _autoBreakEnabled.value = enabled
        settingsRepository.setAutoBreakEnabled(enabled)
    }
    
    fun setBreakDurationMinutes(minutes: Int) {
        _breakDurationMinutes.value = minutes
        settingsRepository.setBreakDurationMinutes(minutes)
    }
    

    
    private val _is24HourFormat = MutableStateFlow(settingsRepository.getIs24HourFormat())
    val is24HourFormat: StateFlow<Boolean> = _is24HourFormat
    
    fun setIs24HourFormat(enabled: Boolean) {
        _is24HourFormat.value = enabled
        settingsRepository.setIs24HourFormat(enabled)
    }
    
    private val _colorPairIndex = MutableStateFlow(settingsRepository.getColorPairIndex())
    val colorPairIndex: StateFlow<Int> = _colorPairIndex
    
    fun setColorPairIndex(index: Int) {
        _colorPairIndex.value = index
        settingsRepository.setColorPairIndex(index)
    }
}
