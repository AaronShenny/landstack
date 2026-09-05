# Phase 1: Backend Foundation

## Overview
This document outlines the Phase 1 implementation of the SIH26014 Land Stack system backend. The objective is to establish a solid Spring Boot foundation with necessary technical boundaries, without implementing premature business logic or complete domain schemas.

## Technology Choices
- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.3.3
- **Build Tool**: Maven
- **Database**: PostgreSQL with PostGIS extension (via Docker)
- **Migrations**: Flyway

## Project Structure
The modular-monolith structure has been established with the following packages in `src/main/java/in/landstack/`:
- `api/` - External HTTP/API boundaries, REST controllers, DTOs.
- `domain/` - Core business model (to be implemented in Phase 2).
- `application/` - Use-case layer, orchestration.
- `infrastructure/` - Technical details, database persistence, external clients.
- `interoperability/` - Future Adapter Engine and State Adapter boundaries.
- `gis/` - Spatial operations, PostGIS integration boundaries.
- `security/` - Authentication, authorization boundaries.
- `audit/` - Future audit trail boundaries.
- `administration/` - Admin UI APIs for configuration-driven State Adapters.

## Database & Local Development
PostgreSQL and PostGIS are run locally via Docker Compose.

**To start the database:**
```bash
docker-compose up -d
```

**Environment Variables:**
The application connects using environment variables. See `.env.example` for the required keys (e.g., `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`). Do NOT commit actual credentials.

## Application Execution
To run the Spring Boot application locally:
```bash
./mvnw spring-boot:run
```
For testing:
```bash
./mvnw test
```

## Observability & Health
Spring Boot Actuator is configured. The health endpoint can be verified at:
- `GET /actuator/health`

## Migrations
Flyway is configured to run automatically. Migration scripts are located in `src/main/resources/db/migration/`. `V1__initial_database_setup.sql` handles initial database setup such as enabling the PostGIS extension.

## APIs & Documentation
- **API Versioning**: Established at `/api/v1` via `in.landstack.api.BaseController`.
- **OpenAPI**: Swagger/OpenAPI support is integrated via `springdoc-openapi` (reachable at `/swagger-ui.html` and `/api-docs` when running).
- **Error Handling**: A global exception handler (`GlobalExceptionHandler`) provides consistent API error responses.
- **Validation**: Jakarta Bean Validation is available for DTOs.
