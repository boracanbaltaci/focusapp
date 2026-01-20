# Focus App

A full-stack focus tracking application with a Spring Boot backend and Android Kotlin frontend.

## Project Structure

This is a monorepo containing:

- **`backend/`** - Spring Boot REST API (Java 17)
- **`android-app/`** - Android client app (Kotlin, Jetpack Compose)

## Quick Start

### Backend

```bash
cd backend
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

See [backend/README.md](backend/README.md) for detailed setup instructions.

### Android App

1. Configure the backend URL in `android-app/app/build.gradle.kts`
2. Open the `android-app` directory in Android Studio
3. Run the app on an emulator or device

See [android-app/README.md](android-app/README.md) for detailed setup instructions.

## Environment Variables

### Backend

The backend requires the following environment variables for production:

- `JWT_SECRET` - Secret key for JWT token generation (required in production)
- `JDBC_DATABASE_URL` - Database connection URL (default: H2 in-memory)
- `JDBC_DATABASE_DRIVER` - Database driver class
- `JDBC_DATABASE_USERNAME` - Database username
- `JDBC_DATABASE_PASSWORD` - Database password
- `PORT` - Server port (default: 8080)

### Android App

The Android app's backend URL is configured in `android-app/app/build.gradle.kts`:

```kotlin
buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
```

Change this to point to your backend server.

## Features

### Backend

- JWT-based authentication
- User management with BCrypt password hashing
- Session tracking (work sessions and breaks)
- Weekly and hourly statistics aggregation
- H2 database (development) with PostgreSQL support (production)
- RESTful API with proper validation and error handling

### Android App

- Modern UI with Jetpack Compose
- Glassy design with translucent cards
- Login and registration
- Session timer with start/stop functionality
- Weekly progress visualization
- Customizable settings (clock type, style, language, background)
- Secure token storage with DataStore

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### Sessions (Authentication Required)

- `POST /api/sessions/start` - Start a new session
- `PUT /api/sessions/{id}/end` - End a session
- `GET /api/sessions/stats/weekly` - Get weekly statistics
- `GET /api/sessions/stats/hourly` - Get hourly statistics

## Development

### Running Tests

Backend:
```bash
cd backend
mvn test
```

Android:
```bash
cd android-app
./gradlew test
```

### Building for Production

Backend:
```bash
cd backend
mvn clean package -DskipTests
```

Android:
```bash
cd android-app
./gradlew assembleRelease
```

## CI/CD

GitHub Actions workflow (`.github/workflows/ci.yml`) automatically:

- Builds the backend with Maven
- Runs backend tests
- Builds the Android app with Gradle
- Runs Android tests

The workflow runs on pushes and pull requests to `main` and `develop` branches.

## Tech Stack

### Backend

- Java 17
- Spring Boot 3.2.1
- Spring Data JPA
- Spring Security
- JWT (jjwt)
- H2 / PostgreSQL
- Maven

### Android

- Kotlin
- Jetpack Compose (Material 3)
- Retrofit + Moshi
- DataStore
- Coroutines
- MVVM Architecture
- Gradle

## Security Notes

- **Never commit secrets** - Use environment variables for sensitive data
- The default JWT secret is for development only
- Change `JWT_SECRET` in production
- Use HTTPS in production
- The H2 console is enabled for development only

## License

This project is open source and available under the MIT License.