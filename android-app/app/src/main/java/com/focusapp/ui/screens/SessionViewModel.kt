package com.focusapp.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusapp.data.model.SessionResponse
import com.focusapp.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SessionViewModel(context: Context) : ViewModel() {
    
    private val sessionRepository = SessionRepository(context)
    
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Idle)
    val sessionState: StateFlow<SessionState> = _sessionState
    
    private val _currentSession = MutableStateFlow<SessionResponse?>(null)
    val currentSession: StateFlow<SessionResponse?> = _currentSession
    
    private val _weeklyStats = MutableStateFlow<Map<String, Long>>(emptyMap())
    val weeklyStats: StateFlow<Map<String, Long>> = _weeklyStats
    
    private val _hourlyStats = MutableStateFlow<Map<Int, Long>>(emptyMap())
    val hourlyStats: StateFlow<Map<Int, Long>> = _hourlyStats
    
    fun startSession(isBreak: Boolean = false) {
        viewModelScope.launch {
            _sessionState.value = SessionState.Loading
            val result = sessionRepository.startSession(isBreak)
            result.fold(
                onSuccess = { session ->
                    _currentSession.value = session
                    _sessionState.value = SessionState.SessionStarted
                },
                onFailure = { error ->
                    _sessionState.value = SessionState.Error(error.message ?: "Failed to start session")
                }
            )
        }
    }
    
    fun endSession() {
        viewModelScope.launch {
            val session = _currentSession.value
            if (session != null) {
                _sessionState.value = SessionState.Loading
                val result = sessionRepository.endSession(session.id)
                result.fold(
                    onSuccess = {
                        _currentSession.value = null
                        _sessionState.value = SessionState.SessionEnded
                        loadWeeklyStats()
                    },
                    onFailure = { error ->
                        _sessionState.value = SessionState.Error(error.message ?: "Failed to end session")
                    }
                )
            }
        }
    }
    
    fun loadWeeklyStats() {
        viewModelScope.launch {
            val result = sessionRepository.getWeeklyStats()
            result.fold(
                onSuccess = { stats ->
                    _weeklyStats.value = stats.dailyDurations
                },
                onFailure = { }
            )
        }
    }
    
    fun loadHourlyStats() {
        viewModelScope.launch {
            val result = sessionRepository.getHourlyStats()
            result.fold(
                onSuccess = { stats ->
                    _hourlyStats.value = stats.hourlyDurations
                },
                onFailure = { }
            )
        }
    }
    
    fun resetState() {
        _sessionState.value = SessionState.Idle
    }
}

sealed class SessionState {
    object Idle : SessionState()
    object Loading : SessionState()
    object SessionStarted : SessionState()
    object SessionEnded : SessionState()
    data class Error(val message: String) : SessionState()
}
