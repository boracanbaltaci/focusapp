package com.focusapp.data.api

import android.content.Context
import com.focusapp.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client singleton for Focus App backend API.
 * 
 * Configuration:
 * - Base URL: Configured in BuildConfig.BASE_URL (default: http://10.0.2.2:8080/ for emulator)
 * - Authentication: Bearer token via Authorization header
 * - Logging: Enabled in debug builds
 * 
 * To change the base URL:
 * 1. Edit android-app/app/build.gradle.kts
 * 2. Modify the buildConfigField in defaultConfig section
 * 3. For production: buildConfigField("String", "BASE_URL", "\"https://your-backend.com/\"")
 * 4. Rebuild the app
 */
object RetrofitClient {
    
    // Bearer token storage - in a real app, this should be stored securely (e.g., EncryptedSharedPreferences)
    private var authToken: String? = null
    
    /**
     * Set the bearer token for API authentication.
     * Call this after user login or when token is retrieved.
     * 
     * @param token JWT or bearer token for authentication
     */
    fun setAuthToken(token: String?) {
        authToken = token
    }
    
    /**
     * Get the current auth token.
     */
    fun getAuthToken(): String? = authToken
    
    /**
     * Create an OkHttpClient with authentication and logging interceptors.
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
            
            // Add Authorization header if token is available
            authToken?.let { token ->
                requestBuilder.header("Authorization", "Bearer $token")
            }
            
            // Add common headers
            requestBuilder
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
            
            val request = requestBuilder.build()
            chain.proceed(request)
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Retrofit instance configured with base URL and OkHttp client.
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * FocusApiService instance for making API calls.
     */
    val apiService: FocusApiService by lazy {
        retrofit.create(FocusApiService::class.java)
    }
}
