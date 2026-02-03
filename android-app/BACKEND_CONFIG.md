# Backend Configuration

## Base URL Configuration

The backend base URL is centrally configured in the app's build configuration.

### Location

The base URL is defined in:
```
android-app/app/build.gradle.kts
```

### Default Value

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080\"")
```

**Default:** `http://10.0.2.2:8080` (for Android emulator)

The IP address `10.0.2.2` is a special alias that Android emulators use to access the host machine's localhost.

### Accessing in Code

The base URL is accessed via `BuildConfig.API_BASE_URL` in:
```
android-app/app/src/main/java/com/focusapp/data/network/RetrofitClient.kt
```

### Changing the Base URL

There are several ways to change the base URL:

#### Option 1: Modify build.gradle.kts (Permanent)

Edit `android-app/app/build.gradle.kts`:

```kotlin
buildTypes {
    debug {
        buildConfigField("String", "API_BASE_URL", "\"http://your-backend-url:port\"")
    }
    release {
        buildConfigField("String", "API_BASE_URL", "\"https://production-url.com\"")
    }
}
```

#### Option 2: Using Gradle Properties (Flexible)

1. Create/edit `gradle.properties`:
```properties
apiBaseUrl=http://your-backend-url:port
```

2. Modify `build.gradle.kts` to read from properties:
```kotlin
val apiBaseUrl: String = project.findProperty("apiBaseUrl") as String? ?: "http://10.0.2.2:8080"

buildTypes {
    debug {
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }
}
```

3. Override at build time:
```bash
./gradlew assembleDebug -PapiBaseUrl=http://custom-url:8080
```

#### Option 3: Using BuildConfig Variants

Create different product flavors:

```kotlin
flavorDimensions += "environment"
productFlavors {
    create("dev") {
        dimension = "environment"
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080\"")
    }
    create("prod") {
        dimension = "environment"
        buildConfigField("String", "API_BASE_URL", "\"https://api.production.com\"")
    }
}
```

### Testing Different URLs

#### Physical Device
- Use your computer's local network IP address: `http://192.168.x.x:8080`
- Find your IP: `ipconfig` (Windows) or `ifconfig` (Mac/Linux)

#### Emulator
- Use `http://10.0.2.2:8080` to access host machine's localhost

#### Production
- Use your production server URL: `https://api.yourdomain.com`

## API Endpoints

The following endpoints are configured:

- `POST /api/sessions` - Create a new focus session
- `POST /api/sessions/{id}/complete` - Mark a session as completed  
- `PUT /api/sessions/{id}` - Update a session

## Authentication

The app supports Bearer token authentication.

### Setting the Auth Token

```kotlin
import com.focusapp.data.network.RetrofitClient

// Set token
RetrofitClient.setAuthToken("your-jwt-token")

// Clear token
RetrofitClient.setAuthToken(null)
```

The token is automatically included in all API requests as:
```
Authorization: Bearer <token>
```

## Network Configuration Details

### HTTP Client Configuration

Located in `RetrofitClient.kt`:

- **Timeouts:**
  - Connect: 30 seconds
  - Read: 30 seconds
  - Write: 30 seconds

- **Logging:** 
  - Enabled in debug builds (full request/response body)
  - Disabled in release builds

### Request/Response Format

All API requests and responses use JSON format with Gson serialization.

### Session Data Sync

When a focus session is completed (either automatically or via the finish button):

1. The session is saved locally to SharedPreferences
2. The session is sent to the backend API asynchronously
3. Backend failures are logged but don't affect local functionality
4. The app works fully offline - backend sync is optional

This ensures the app remains functional even without backend connectivity.
