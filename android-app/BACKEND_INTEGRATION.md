# Backend Integration Configuration

This document describes how the Focus App integrates with a backend API and how to configure the backend URL.

## Base URL Configuration

The backend base URL is configured via BuildConfig and can be changed in multiple ways:

### Default Configuration
By default, the app uses `http://10.0.2.2:8080` which is the Android emulator's way to access localhost on the host machine.

### Methods to Change the Base URL

#### 1. Via gradle.properties
Add the following line to your `gradle.properties` file:
```properties
BASE_URL=http://your-backend-server:port
```

#### 2. Via Command Line
Build with a custom URL using:
```bash
./gradlew assembleDebug -PBASE_URL=http://your-backend-server:port
```

#### 3. Via build.gradle.kts (Permanent Change)
Edit `android-app/app/build.gradle.kts` and modify the buildConfigField:
```kotlin
buildConfigField("String", "BASE_URL", "\"http://your-backend-server:port\"")
```

## Backend API Endpoints

The app sends focus session data to the following endpoints:

### 1. Create Session
- **Endpoint**: `POST /api/sessions`
- **When**: When user starts a timer session
- **Request Body**:
  ```json
  {
    "startTime": "2024-01-01T12:00:00",
    "endTime": null,
    "durationSeconds": null,
    "isBreak": false
  }
  ```
- **Response**: Session object with server-assigned ID

### 2. Complete Session
- **Endpoint**: `POST /api/sessions/{id}/complete`
- **When**: 
  - When timer reaches zero (automatic completion)
  - When user taps the finish button (right-side "bitir" button)
- **Response**: Updated session object with completion status

### 3. Update Session
- **Endpoint**: `PUT /api/sessions/{id}`
- **When**: For future use to update session details
- **Request Body**: Updated session data

## Authentication

All API requests support Bearer token authentication via the `Authorization` header:
```
Authorization: Bearer <your-token>
```

To set the bearer token in code:
```kotlin
RetrofitClient.setBearerToken("your-jwt-token")
```

## Offline Support

The app maintains full offline functionality:
- All sessions are saved to local Room database first
- Backend sync happens asynchronously
- If backend is unavailable, the app continues to work offline
- No data is lost if backend sync fails

## Network Error Handling

- Network errors are logged but don't interrupt the user experience
- Sessions are always saved locally first
- Backend sync failures are logged with warning level
- Check Logcat for network sync status:
  ```
  SessionRepository: Session created on backend with ID: 123
  SessionRepository: Backend not available, session saved locally only
  ```

## Testing the Integration

1. **Start Backend Server** (if you have one):
   ```bash
   # Start your backend on port 8080
   # From Android emulator, it will be accessible at http://10.0.2.2:8080
   ```

2. **Run the App**:
   ```bash
   cd android-app
   ./gradlew installDebug
   ```

3. **Monitor Network Calls**:
   - Check Logcat for network logs
   - Look for `SessionRepository` tag
   - HTTP requests/responses are logged by OkHttp interceptor

## Code Structure

```
data/network/
├── ApiConfig.kt           # Base URL and endpoint constants
├── ApiModels.kt           # Request/Response data classes
├── AuthInterceptor.kt     # Bearer token authentication
├── FocusApiService.kt     # Retrofit API interface
└── RetrofitClient.kt      # Retrofit singleton instance

data/repository/
└── SessionRepository.kt   # Handles both local DB and API calls
```

## Dependencies Added

```kotlin
// Retrofit for networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
```

## Troubleshooting

### Backend Connection Issues
- **Error**: `Backend not available, session saved locally only`
- **Solution**: 
  - Verify backend is running
  - Check BASE_URL is correct
  - Use `http://10.0.2.2:8080` for emulator
  - Use actual IP for physical device

### SSL/TLS Errors
- For development, ensure backend uses HTTP not HTTPS
- Or configure proper SSL certificates

### Authentication Errors
- **Error**: 401 Unauthorized
- **Solution**: Set bearer token using `RetrofitClient.setBearerToken(token)`

## Future Enhancements

Potential improvements to consider:
- Retry logic for failed network requests
- Queue system for offline session sync
- Background sync service
- User authentication flow
- Session conflict resolution
