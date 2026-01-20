package com.focusapp.data.repository

import com.focusapp.data.api.RetrofitClient
import com.focusapp.data.model.*

class AuthRepository {
    
    private val api = RetrofitClient.apiService
    
    suspend fun register(username: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.register(RegisterRequest(username, password))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(username, password))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message() ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
