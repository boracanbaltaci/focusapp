package com.focusapp.data.network

import com.focusapp.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client for making API calls to the focus session backend.
 * 
 * Configuration:
 * - Base URL: Configured via BuildConfig.API_BASE_URL (default: http://10.0.2.2:8080 for emulator)
 * - Authentication: Bearer token support via AuthInterceptor
 * - Logging: HTTP request/response logging in debug builds
 * 
 * To change the base URL:
 * - Update BuildConfig.API_BASE_URL in app/build.gradle.kts
 * - Or set via gradle property: apiBaseUrl=<url>
 */
object RetrofitClient {
    
    // Bearer token for authentication - can be set via setAuthToken()
    private var authToken: String? = null
    
    /**
     * Set the authentication token for API requests
     * @param token Bearer token (without "Bearer " prefix)
     */
    fun setAuthToken(token: String?) {
        authToken = token
    }
    
    /**
     * Get the current authentication token
     */
    fun getAuthToken(): String? = authToken
    
    /**
     * Interceptor to add Authorization header with Bearer token
     */
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request()
        val token = authToken
        
        val newRequest = if (token != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        
        chain.proceed(newRequest)
    }
    
    /**
     * Logging interceptor for debugging (only in debug builds)
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    
    /**
     * OkHttp client with interceptors
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * Retrofit instance
     */
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    /**
     * Get the FocusApiService instance
     */
    val apiService: FocusApiService = retrofit.create(FocusApiService::class.java)
}
