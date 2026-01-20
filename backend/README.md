# Focus App Backend

Spring Boot REST API for the Focus tracking application.

## Technologies

- Java 17
- Spring Boot 3.2.1
- Spring Data JPA
- Spring Security with JWT
- H2 Database (dev) / PostgreSQL (production)
- Maven

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Running Locally

1. Navigate to the backend directory:
```bash
cd backend
```

2. Build the project:
```bash
mvn clean package
```

3. Run the application:
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

## Environment Variables

The following environment variables can be set to configure the application:

- `PORT`: Server port (default: 8080)
- `JDBC_DATABASE_URL`: Database connection URL (default: jdbc:h2:mem:focusdb)
- `JDBC_DATABASE_DRIVER`: Database driver class (default: org.h2.Driver)
- `JDBC_DATABASE_USERNAME`: Database username (default: sa)
- `JDBC_DATABASE_PASSWORD`: Database password (default: empty)
- `JWT_SECRET`: Secret key for JWT token generation (default: development key - **must be changed in production**)

### PostgreSQL Configuration

To use PostgreSQL instead of H2, set the following environment variables:

```bash
export JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/focusdb
export JDBC_DATABASE_DRIVER=org.postgresql.Driver
export JDBC_DATABASE_USERNAME=postgres
export JDBC_DATABASE_PASSWORD=yourpassword
export JWT_SECRET=your-very-long-and-secure-secret-key
```

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register a new user
  ```json
  {
    "username": "user123",
    "password": "password"
  }
  ```

- `POST /api/auth/login` - Login and get JWT token
  ```json
  {
    "username": "user123",
    "password": "password"
  }
  ```

### Sessions (Requires Authentication)

Include the JWT token in the Authorization header: `Authorization: Bearer <token>`

- `POST /api/sessions/start` - Start a new session
  ```json
  {
    "isBreak": false
  }
  ```

- `PUT /api/sessions/{id}/end` - End a session

- `GET /api/sessions/stats/weekly` - Get weekly statistics (last 7 days)

- `GET /api/sessions/stats/hourly` - Get hourly statistics (last 24 hours)

## Database Console (H2)

When running with H2, you can access the database console at:
`http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:mem:focusdb`
- Username: `sa`
- Password: (leave empty)

## Running Tests

```bash
mvn test
```

## Building for Production

```bash
mvn clean package -DskipTests
```

The JAR file will be created in the `target/` directory.
