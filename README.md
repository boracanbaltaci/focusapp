# Focus App

A simple, offline-only focus tracking Android application built with Kotlin and Jetpack Compose.

## Project Structure

This repository contains:

- **`android-app/`** - Android client app (Kotlin, Jetpack Compose, Room Database)

## Quick Start

### Android App

1. Open the `android-app` directory in Android Studio
2. Build and run the app on an emulator or device

The app works completely offline with no backend required.

## Features

### Android App

- Modern UI with Jetpack Compose
- Glassy design with translucent cards
- Session timer with start/stop functionality (work sessions and breaks)
- Weekly progress visualization
- All data stored locally on device using Room Database
- Customizable settings (clock type, style, language, background)
- No login required - works completely offline

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
- Coroutines
- MVVM Architecture
- Gradle

## License

This project is open source and available under the MIT License.
