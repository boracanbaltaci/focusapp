package com.focusapp.data.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor for adding Bearer token authentication to API requests
 * 
 * This interceptor adds the "Authorization: Bearer <token>" header to all outgoing requests.
 * 
 * Usage:
 * - Set the bearer token via setBearerToken() method
 * - Token is automatically added to all requests
 * - If no token is set, requests proceed without Authorization header
 */
class AuthInterceptor : Interceptor {
    
    @Volatile
    private var bearerToken: String? = null
    
    /**
     * Set the bearer token to be used for authentication
     * @param token The JWT or bearer token string (without "Bearer " prefix)
     */
    fun setBearerToken(token: String?) {
        this.bearerToken = token
    }
    
    /**
     * Get the current bearer token
     * @return The current bearer token or null if not set
     */
    fun getBearerToken(): String? = bearerToken
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // If no token is set, proceed with the original request
        val token = bearerToken
        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }
        
        // Add Authorization header with Bearer token
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        return chain.proceed(authenticatedRequest)
    }
}
