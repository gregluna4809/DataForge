# DataForge

DataForge is an enterprise-style data quality, analytics, and governance platform.

## Backend Foundation

The backend is located in `backend/` and uses:

- Java 21
- Spring Boot 3
- Maven
- PostgreSQL
- Flyway
- Spring Security
- JWT authentication

## Project Structure

```text
backend/
  src/main/java/com/dataforge/
    DataForgeApplication.java
    auth/
    common/
      health/
      protectedtest/
    security/
    users/
  src/main/resources/
    application.yml
    db/migration/
```

The package structure is intentionally narrow for the first milestone. Additional modules such as `auth`, `users`, `datasets`, `ingestion`, `profiling`, `rules`, `reports`, `ai`, and `security` should be added when those features are implemented.

## Configuration

PostgreSQL settings are configured through environment variables with local development defaults:

```text
DATAFORGE_DB_URL=jdbc:postgresql://localhost:5432/dataforge
DATAFORGE_DB_USERNAME=dataforge
DATAFORGE_DB_PASSWORD=dataforge
DATAFORGE_SERVER_PORT=8080
DATAFORGE_JWT_SECRET=dataforge-local-development-secret-change-before-production-64chars
DATAFORGE_JWT_EXPIRATION_MINUTES=60
```

Flyway is enabled and reads migrations from:

```text
classpath:db/migration
```

## Run The Backend

From the `backend/` directory:

```powershell
mvn spring-boot:run
```

Health check:

```text
GET http://localhost:8080/api/health
```

Authentication endpoints:

```text
POST http://localhost:8080/api/auth/register
POST http://localhost:8080/api/auth/login
GET  http://localhost:8080/api/protected
```

Use the login or registration response token as:

```text
Authorization: Bearer <accessToken>
```

## Build

From the `backend/` directory:

```powershell
mvn clean verify
```
