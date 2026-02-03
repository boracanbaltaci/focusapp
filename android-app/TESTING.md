# Testing Backend Integration

## Overview

This document describes how to test the backend integration for focus session data upload.

## Prerequisites

1. **Backend Server Running**
   - The backend must be running on `http://localhost:8080` (or configured URL)
   - Backend should implement the API endpoints described in api-docs.json

2. **Android Emulator or Device**
   - Emulator: Use the default URL `http://10.0.2.2:8080`
   - Physical Device: Update the base URL to your computer's local IP

## Testing Steps

### 1. Start the Backend Server

Ensure your backend server is running and accessible at the configured URL.

### 2. Build and Install the App

```bash
cd android-app
./gradlew installDebug
```

### 3. Test Session Creation and Completion

#### Automatic Completion (Timer Runs Out)

1. Open the app on the emulator/device
2. Navigate to the Timer screen (swipe left from the clock screen)
3. Tap on the timer to select a duration (e.g., 5 minutes)
4. Tap the green play button to start the timer
5. Wait for the timer to complete (or speed up time in emulator settings)
6. When the timer reaches 00:00, the session should be:
   - Saved locally to SharedPreferences
   - Sent to backend via POST /api/sessions
   - Marked as completed via POST /api/sessions/{id}/complete

#### Manual Completion (Finish Button)

1. Navigate to the Timer screen
2. Select a duration and start the timer
3. While the timer is running, tap the gray square button on the right (finish button)
4. The session should be:
   - Saved locally with the elapsed time
   - Sent to backend with the actual duration used

### 4. Monitor Network Traffic

#### Using Android Studio Logcat

1. Open Android Studio
2. Open Logcat (View → Tool Windows → Logcat)
3. Filter by "StatisticsRepository" or "SessionRepository" to see backend sync logs
4. Look for messages like:
   - "Session created on backend with ID: X"
   - "Session X marked as completed on backend"
   - "Failed to create session on backend: 404" (if backend is not running)

#### Using OkHttp Logging

In debug builds, full HTTP request/response logging is enabled. Look for:
- Request headers and body
- Response codes and body
- Network errors

Example log output:
```
D/OkHttp: --> POST http://10.0.2.2:8080/api/sessions
D/OkHttp: Content-Type: application/json
D/OkHttp: {"startTime":"2026-02-03T21:30:00","endTime":"2026-02-03T21:35:00","durationSeconds":300,"isBreak":false}
D/OkHttp: --> END POST
D/OkHttp: <-- 200 OK http://10.0.2.2:8080/api/sessions
D/OkHttp: {"id":123,"startTime":"2026-02-03T21:30:00","endTime":"2026-02-03T21:35:00","durationSeconds":300,"isBreak":false}
D/OkHttp: <-- END HTTP
```

### 5. Verify Backend Data

Check your backend database or API to verify:
1. Session was created with correct data
2. Session was marked as completed
3. All fields (startTime, endTime, durationSeconds, isBreak) are correct

## Testing Bearer Token Authentication

### Set Auth Token

To test with authentication:

1. Add code to set the token (e.g., in MainActivity or after login):

```kotlin
import com.focusapp.data.network.RetrofitClient

RetrofitClient.setAuthToken("your-test-token-here")
```

2. The token will be automatically included in all requests:
```
Authorization: Bearer your-test-token-here
```

### Clear Auth Token

```kotlin
RetrofitClient.setAuthToken(null)
```

## Offline Behavior

The app is designed to work offline. To test:

1. Turn off Wi-Fi/Data on the device
2. Complete a focus session
3. Check Logcat for error logs (should see network errors but app continues working)
4. Session should still be saved locally
5. Turn on Wi-Fi/Data
6. Future sessions should sync successfully

## Common Issues

### "Failed to create session on backend: 404"

**Cause:** Backend is not running or URL is incorrect

**Solution:**
- Verify backend is running: `curl http://localhost:8080/api/sessions`
- Check the base URL in build.gradle.kts
- For physical device, use your computer's local IP instead of 10.0.2.2

### "Failed to create session on backend: 401"

**Cause:** Authentication required but no token set

**Solution:**
- Set a valid bearer token using `RetrofitClient.setAuthToken(token)`
- Or configure backend to accept unauthenticated requests for testing

### "Network is unreachable"

**Cause:** Network connectivity issues

**Solution:**
- Verify emulator/device has internet access
- Check firewall settings
- Ensure backend allows connections from the emulator/device

### No logs appearing

**Cause:** Log level filtering

**Solution:**
- Set Logcat filter to "Verbose" or "Debug"
- Remove any tag filters
- Check both "StatisticsRepository" and "SessionRepository" tags

## Expected API Calls

For a completed session, you should see two API calls:

1. **Create Session**
   ```
   POST /api/sessions
   Content-Type: application/json
   
   {
     "startTime": "2026-02-03T21:30:00",
     "endTime": "2026-02-03T21:35:00",
     "durationSeconds": 300,
     "isBreak": false
   }
   ```

2. **Complete Session**
   ```
   POST /api/sessions/{id}/complete
   Authorization: Bearer <token>
   ```

Both calls are made asynchronously and failures don't affect local functionality.

## Performance Testing

To test with multiple sessions:

1. Complete several focus sessions
2. Monitor memory usage (should remain stable)
3. Check backend for all sessions
4. Verify no duplicate sessions are created

## Security Testing

1. **Token Security:** Tokens are stored in memory only (not persisted)
2. **HTTPS:** For production, update base URL to use HTTPS
3. **Error Messages:** Verify sensitive information is not exposed in logs

## Configuration Testing

Test different base URLs:

```bash
# Test with custom URL
./gradlew installDebug -PapiBaseUrl=http://192.168.1.100:8080

# Or edit build.gradle.kts and rebuild
```

## Automated Testing

For automated integration tests, consider:

1. Mock server using WireMock or MockWebServer
2. Test success scenarios (200 responses)
3. Test failure scenarios (404, 500, network errors)
4. Verify offline functionality

Example test structure:
```kotlin
@Test
fun `session saved locally when backend fails`() {
    // Setup mock server to return 500
    // Complete a session
    // Verify session exists in SharedPreferences
    // Verify error was logged but didn't crash
}
```
