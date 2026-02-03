# Focus App

A focus tracking Android application built with Kotlin and Jetpack Compose, with optional backend integration.

## Project Structure

This repository contains:

- **`android-app/`** - Android client app (Kotlin, Jetpack Compose, Room Database)
- **`api-docs.json`** - OpenAPI specification for the backend API

## Quick Start

### Android App

1. Open the `android-app` directory in Android Studio
2. Build and run the app on an emulator or device

The app works completely offline with no backend required. Backend integration is optional and enhances the app with cloud sync capabilities.

## Features

### Android App

- Modern UI with Jetpack Compose
- Glassy design with translucent cards
- Session timer with start/stop functionality (work sessions and breaks)
- Weekly progress visualization
- All data stored locally on device using Room Database
- **Backend Integration (Optional)**: Sync sessions to a backend server
- Customizable settings (clock type, style, language, background)
- No login required - works completely offline

## Backend Integration

The app includes optional backend integration that sends focus session data to a REST API.

### Configuration

The backend base URL defaults to `http://10.0.2.2:8080` (Android emulator localhost).

To change the backend URL:
1. **Via gradle.properties**: Add `BASE_URL=http://your-server:port`
2. **Via command line**: `./gradlew assembleDebug -PBASE_URL=http://your-server:port`
3. **Via build.gradle.kts**: Edit the `buildConfigField` in `app/build.gradle.kts`

See [android-app/BACKEND_INTEGRATION.md](android-app/BACKEND_INTEGRATION.md) for detailed configuration instructions.

### API Endpoints

The app communicates with these endpoints:
- `POST /api/sessions` - Create a new session when timer starts
- `POST /api/sessions/{id}/complete` - Mark session complete (timer finish or manual stop)
- `PUT /api/sessions/{id}` - Update session (for future use)

See [api-docs.json](api-docs.json) for the complete OpenAPI specification.

### Offline Support

The app maintains full offline functionality:
- Sessions are always saved to local Room database first
- Backend sync happens asynchronously
- If backend is unavailable, app continues to work offline
- No data loss occurs if backend sync fails

## Development

### Running Tests

Android:
```bash
cd android-app
./gradlew test
```

### Building for Production

Android:
```bash
cd android-app
./gradlew assembleRelease
```

## Tech Stack

### Android

- Kotlin
- Jetpack Compose (Material 3)
- Room Database (local storage)
- Retrofit (HTTP client)
- OkHttp (networking)
- Coroutines
- MVVM Architecture
- Gradle

## License

This project is open source and available under the MIT License.
