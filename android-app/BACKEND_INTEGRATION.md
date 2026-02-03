# Backend Integration Configuration

## Base URL Configuration

The app is configured to communicate with a backend API. The base URL is centralized and can be easily changed.

### Current Configuration

**Default Base URL:** `http://10.0.2.2:8080`
- This URL is specifically configured for Android emulator access
- `10.0.2.2` is the special alias to the host machine's localhost
- Perfect for local development with a backend running on port 8080

### How to Change the Base URL

The base URL is defined in `app/build.gradle.kts` using BuildConfig fields:

```kotlin
buildTypes {
    release {
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080\"")
    }
    debug {
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080\"")
    }
}
```

#### Option 1: Edit build.gradle.kts directly

1. Open `android-app/app/build.gradle.kts`
2. Find the `buildTypes` section
3. Change the BASE_URL value in the `buildConfigField` line
4. Sync Gradle

Examples:
```kotlin
// For production server
buildConfigField("String", "BASE_URL", "\"https://api.yourapp.com\"")

// For different local port
buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000\"")

// For physical device testing (use your computer's IP)
buildConfigField("String", "BASE_URL", "\"http://192.168.1.100:8080\"")
```

#### Option 2: Use Gradle property

You can also set it via gradle.properties or command line:

```bash
./gradlew assembleDebug -PBASE_URL="http://your-url:port"
```

## API Endpoints

The app implements the following API endpoints for session management:

### 1. Create Session
- **Endpoint:** `POST /api/sessions`
- **Description:** Creates a new focus session
- **Authentication:** Bearer token required
- **Request Body:**
  ```json
  {
    "startTime": "2024-01-01T10:00:00",
    "endTime": "2024-01-01T10:25:00",
    "durationSeconds": 1500,
    "isBreak": false
  }
  ```
- **Response:**
  ```json
  {
    "id": 123,
    "userId": 1,
    "startTime": "2024-01-01T10:00:00",
    "endTime": "2024-01-01T10:25:00",
    "durationSeconds": 1500,
    "isBreak": false,
    "createdAt": "2024-01-01T10:25:00",
    "updatedAt": "2024-01-01T10:25:00"
  }
  ```

### 2. Complete Session
- **Endpoint:** `POST /api/sessions/{id}/complete`
- **Description:** Marks a session as completed
- **Authentication:** Bearer token required
- **Path Parameter:** `id` - Session ID
- **Response:** FocusSessionResponse (same as create)

### 3. Update Session
- **Endpoint:** `PUT /api/sessions/{id}`
- **Description:** Updates an existing session
- **Authentication:** Bearer token required
- **Path Parameter:** `id` - Session ID
- **Request Body:** FocusSessionRequest (same as create)
- **Response:** FocusSessionResponse (same as create)

## Authentication

The app supports Bearer token authentication:

### Setting an Authentication Token

Tokens are managed by the `TokenManager` class and stored in SharedPreferences.

To set a token programmatically:

```kotlin
import com.focusapp.data.network.TokenManager

val tokenManager = TokenManager(context)
tokenManager.saveToken("your-jwt-token-here")
```

### How Authentication Works

1. Tokens are stored in SharedPreferences using `TokenManager`
2. `RetrofitClient` automatically adds the token to all API requests via an interceptor
3. All requests include `Authorization: Bearer <token>` header
4. If no token is set, requests will be sent without authentication

### Token Management Methods

```kotlin
// Save token
tokenManager.saveToken("jwt-token")

// Get token
val token = tokenManager.getToken()

// Check if token exists
val hasToken = tokenManager.hasToken()

// Clear token
tokenManager.clearToken()
```

## Session Sync Behavior

### When Sessions are Sent to Backend

Focus session data is automatically sent to the backend when a session is completed:

1. **Timer expires:** When the countdown timer reaches 0:00
2. **User taps "bitir" button:** The right-side finish button on the timer screen

### Sync Implementation

The sync happens in `SessionRepository.endSession()`:
1. Session is saved locally in Room database (offline-first)
2. Backend API is called asynchronously
3. If backend call fails, the session is still saved locally (no data loss)
4. Logs are written for debugging (check Logcat for "SessionRepository" tag)

### Offline Support

The app maintains offline-first behavior:
- Sessions are always saved locally first
- Backend sync happens in the background
- Failed syncs are logged but don't affect local functionality
- App works normally even if backend is unreachable

## Network Configuration

### Retrofit Setup

The app uses Retrofit for HTTP networking with the following configuration:

- **Base URL:** Set via BuildConfig (see above)
- **Converter:** Gson for JSON serialization
- **Logging:** HTTP request/response logging in debug builds
- **Timeouts:**
  - Connect: 30 seconds
  - Read: 30 seconds
  - Write: 30 seconds

### Files Structure

```
com.focusapp.data.network/
├── FocusApiService.kt       # Retrofit API interface
├── RetrofitClient.kt        # Retrofit configuration & singleton
└── TokenManager.kt          # Authentication token storage
```

## Testing the Integration

### Prerequisites

1. Backend server running on `http://localhost:8080` (or your configured URL)
2. Backend implementing the session endpoints described above
3. Android emulator running (or physical device with appropriate IP configuration)

### Manual Testing Steps

1. **Start the app** on the emulator
2. **Optional:** Set an auth token if your backend requires it:
   ```kotlin
   // Add this temporarily in MainActivity.onCreate()
   TokenManager(this).saveToken("your-test-token")
   ```
3. **Navigate to the timer screen** (swipe left from home)
4. **Start a timer** and let it complete OR tap the "bitir" button
5. **Check Logcat** for network requests:
   ```
   Filter by tag: SessionRepository
   Look for: "Session created on backend" and "Session completed on backend"
   ```
6. **Verify on backend** that the session was created and completed

### Debugging Network Requests

Enable detailed network logging by checking Logcat with filter "OkHttp":
```
Filter: OkHttp
```

This will show:
- Request URLs and headers
- Request/response bodies
- HTTP status codes
- Network errors

## Troubleshooting

### Common Issues

**Problem:** Network requests fail with "Unable to resolve host"
- **Solution:** Ensure backend is running and accessible
- For emulator: Use `http://10.0.2.2:PORT` (not localhost)
- For physical device: Use your computer's IP address

**Problem:** 401 Unauthorized errors
- **Solution:** Ensure auth token is set correctly
- Check token format (should be "Bearer <token>")
- Verify token is valid on backend

**Problem:** Connection timeout
- **Solution:** Check firewall settings
- Ensure backend port is accessible
- Try increasing timeout values in RetrofitClient

**Problem:** Sessions not syncing
- **Solution:** Check Logcat for errors
- Verify backend endpoints match API specification
- Ensure BASE_URL is correct

## Future Enhancements

Potential improvements to backend integration:

1. **Retry Logic:** Implement exponential backoff for failed requests
2. **Sync Queue:** Queue failed syncs and retry when connection is restored
3. **Conflict Resolution:** Handle sync conflicts if user has multiple devices
4. **Background Sync:** Use WorkManager for background session uploads
5. **Batch Upload:** Send multiple sessions in a single request
