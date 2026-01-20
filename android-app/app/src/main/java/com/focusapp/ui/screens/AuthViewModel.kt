package com.focusapp.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusapp.data.api.RetrofitClient
import com.focusapp.data.repository.AuthRepository
import com.focusapp.data.repository.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val context: Context) : ViewModel() {
    
    private val authRepository = AuthRepository()
    private val tokenManager = TokenManager(context)
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn
    
    init {
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        viewModelScope.launch {
            tokenManager.token.collect { token ->
                _isLoggedIn.value = token != null
                if (token != null) {
                    RetrofitClient.setAuthToken(token)
                }
            }
        }
    }
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.login(username, password)
            result.fold(
                onSuccess = { response ->
                    if (response.token != null && response.username != null) {
                        tokenManager.saveToken(response.token, response.username)
                        RetrofitClient.setAuthToken(response.token)
                        _authState.value = AuthState.Success(response.message ?: "Login successful")
                        _isLoggedIn.value = true
                    } else {
                        _authState.value = AuthState.Error(response.message ?: "Login failed")
                    }
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }
    
    fun register(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.register(username, password)
            result.fold(
                onSuccess = { response ->
                    if (response.token != null && response.username != null) {
                        tokenManager.saveToken(response.token, response.username)
                        RetrofitClient.setAuthToken(response.token)
                        _authState.value = AuthState.Success(response.message ?: "Registration successful")
                        _isLoggedIn.value = true
                    } else {
                        _authState.value = AuthState.Error(response.message ?: "Registration failed")
                    }
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
            RetrofitClient.setAuthToken(null)
            _isLoggedIn.value = false
            _authState.value = AuthState.Idle
        }
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
