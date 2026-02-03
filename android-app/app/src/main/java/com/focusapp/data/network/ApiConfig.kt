package com.focusapp.data.network

import com.focusapp.BuildConfig

/**
 * API Configuration
 * 
 * BASE_URL is configured via BuildConfig and defaults to http://10.0.2.2:8080 (Android emulator localhost).
 * 
 * To change the base URL:
 * 1. Add BASE_URL property to gradle.properties: BASE_URL=http://your-server:port
 * 2. Or pass via command line: ./gradlew assembleDebug -PBASE_URL=http://your-server:port
 * 3. Or modify the defaultConfig.buildConfigField in app/build.gradle.kts
 */
object ApiConfig {
    /**
     * Base URL for the backend API.
     * Default: http://10.0.2.2:8080 (Android emulator localhost)
     */
    val BASE_URL: String = BuildConfig.BASE_URL
    
    /**
     * API endpoints
     */
    const val SESSIONS_ENDPOINT = "/api/sessions"
    const val COMPLETE_SESSION_ENDPOINT = "/api/sessions/{id}/complete"
    const val UPDATE_SESSION_ENDPOINT = "/api/sessions/{id}"
}
