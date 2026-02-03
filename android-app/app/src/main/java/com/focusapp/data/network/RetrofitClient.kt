package com.focusapp.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client singleton for API communication
 * 
 * Provides a configured Retrofit instance with:
 * - Bearer token authentication support via AuthInterceptor
 * - Request/response logging in debug builds
 * - Gson converter for JSON serialization
 * - Configurable timeouts
 */
object RetrofitClient {
    
    private val authInterceptor = AuthInterceptor()
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Get the FocusApiService instance
     */
    val apiService: FocusApiService by lazy {
        retrofit.create(FocusApiService::class.java)
    }
    
    /**
     * Set the bearer token for API authentication
     * @param token The JWT or bearer token string (without "Bearer " prefix)
     */
    fun setBearerToken(token: String?) {
        authInterceptor.setBearerToken(token)
    }
    
    /**
     * Get the current bearer token
     * @return The current bearer token or null if not set
     */
    fun getBearerToken(): String? = authInterceptor.getBearerToken()
}
