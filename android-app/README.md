# Focus App - Android Client

Android client for the Focus tracking application built with Kotlin and Jetpack Compose.

## Technologies

- Kotlin
- Jetpack Compose (Material 3)
- MVVM Architecture
- Retrofit + Moshi for networking
- DataStore for secure token storage
- Coroutines for async operations

## Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or higher
- Android SDK with API level 34
- Gradle 8.2

## Configuration

### Backend URL

The app connects to the backend API. The default URL is configured for Android Emulator to connect to localhost:

```kotlin
// In app/build.gradle.kts
buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
```

To change the backend URL:

1. Open `android-app/app/build.gradle.kts`
2. Find the `buildConfigField` for `BASE_URL`
3. Update the URL:
   - For emulator connecting to localhost: `http://10.0.2.2:8080/`
   - For physical device on same network: `http://<your-machine-ip>:8080/`
   - For production: `https://your-api-domain.com/`

## Building and Running

### Build Debug APK

```bash
cd android-app
./gradlew assembleDebug
```

The APK will be generated in `app/build/outputs/apk/debug/`.

### Run on Emulator or Device

1. Open the project in Android Studio
2. Select a device/emulator
3. Click "Run" or press Shift+F10

Or from command line:

```bash
./gradlew installDebug
```

## Project Structure

```
app/src/main/java/com/focusapp/
├── data/
│   ├── api/           # Retrofit API interfaces and client
│   ├── model/         # Data models
│   └── repository/    # Repository layer
├── ui/
│   ├── components/    # Reusable UI components
│   ├── screens/       # Screen composables and ViewModels
│   └── theme/         # Theme and styling
└── MainActivity.kt    # Main entry point
```

## Features

### Screens

1. **Login Screen**
   - User registration
   - User login
   - JWT token storage

2. **Home Screen**
   - Start/stop work sessions
   - Start break sessions
   - View weekly progress chart
   - Quick stats preview

3. **Settings Screen**
   - Clock type (Digital/Analog)
   - Style selection
   - Background selection
   - Language selection

### Data Storage

The app uses DataStore Preferences to securely store:
- JWT authentication token
- Username

### UI Design

- Glassy design with translucent cards
- Rounded corners
- Material 3 theming
- Dark color scheme

## Testing

Run unit tests:

```bash
./gradlew test
```

## API Integration

The app communicates with the backend REST API:

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/sessions/start` - Start session
- `PUT /api/sessions/{id}/end` - End session
- `GET /api/sessions/stats/weekly` - Get weekly stats
- `GET /api/sessions/stats/hourly` - Get hourly stats

All authenticated endpoints require a Bearer token in the Authorization header.

## Building for Production

```bash
./gradlew assembleRelease
```

**Note:** For production builds, you'll need to:
1. Configure signing keys in `app/build.gradle.kts`
2. Update the BASE_URL to your production API
3. Enable ProGuard/R8 minification

## Troubleshooting

### Connection Issues

If the app can't connect to the backend:

1. Ensure the backend is running on port 8080
2. For emulator, use `10.0.2.2` instead of `localhost`
3. For physical device, ensure it's on the same network
4. Check firewall settings

### Build Issues

If Gradle sync fails:
1. File → Invalidate Caches / Restart
2. Clean and rebuild: `./gradlew clean build`
