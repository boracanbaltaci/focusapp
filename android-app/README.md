# Focus App - Android Client

Android focus tracking application built with Kotlin and Jetpack Compose. All data is stored locally on the device with no backend required.

## Technologies

- Kotlin
- Jetpack Compose (Material 3)
- MVVM Architecture
- Room Database for local storage
- Coroutines for async operations

## Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or higher
- Android SDK with API level 34
- Gradle 8.2

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
│   ├── local/         # Room database entities and DAOs
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

1. **Home Screen**
   - Start/stop work sessions
   - Start break sessions
   - View weekly progress chart
   - Quick stats preview

2. **Settings Screen**
   - Clock type (Digital/Analog)
   - Style selection
   - Background selection
   - Language selection

### Data Storage

The app uses Room Database to locally store:
- Focus session data (start time, end time, duration)
- Break session data
- Weekly and hourly statistics

All data is stored on the device and persists between app launches.

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

## Building for Production

```bash
./gradlew assembleRelease
```

**Note:** For production builds, you'll need to:
1. Configure signing keys in `app/build.gradle.kts`
2. Enable ProGuard/R8 minification

## Troubleshooting

### Build Issues

If Gradle sync fails:
1. File → Invalidate Caches / Restart
2. Clean and rebuild: `./gradlew clean build`
