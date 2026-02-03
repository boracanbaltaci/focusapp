package com.focusapp.data.network

import android.content.Context
import com.focusapp.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client singleton for Focus App API.
 * 
 * Configuration:
 * - Base URL: Configured via BuildConfig.BASE_URL (default: http://10.0.2.2:8080 for emulator)
 * - Authentication: Bearer token via Authorization header
 * - Logging: Enabled in debug builds
 * 
 * To change the base URL:
 * - Edit build.gradle.kts and update the buildConfigField for BASE_URL
 * - Or set via gradle property: -PBASE_URL="http://your-url:port"
 */
object RetrofitClient {
    
    private var tokenManager: TokenManager? = null
    
    /**
     * Initialize the Retrofit client with context for token management.
     * Must be called before using the API service.
     */
    fun initialize(context: Context) {
        tokenManager = TokenManager(context)
    }
    
    /**
     * Get the configured API service instance.
     */
    fun getApiService(): FocusApiService {
        return retrofit.create(FocusApiService::class.java)
    }
    
    /**
     * Authentication interceptor that adds Bearer token to requests.
     */
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = tokenManager?.getToken()
        
        val requestBuilder = originalRequest.newBuilder()
        
        // Add Authorization header if token is available
        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        
        chain.proceed(requestBuilder.build())
    }
    
    /**
     * Logging interceptor for debugging network requests.
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    
    /**
     * OkHttp client with interceptors and timeout configuration.
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * Retrofit instance configured with base URL and JSON converter.
     * Base URL is set to http://10.0.2.2:8080 for Android emulator access.
     */
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
