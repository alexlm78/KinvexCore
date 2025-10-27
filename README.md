# Kinvex Core - Backend API

Backend API for the Kinvex inventory system developed with Spring Boot.

## Technologies

- **Java 21**
- **Spring Boot 3.2.0**
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence
- **PostgreSQL** - Main database
- **Redis** - Cache and sessions
- **JWT** - Authentication tokens
- **Flyway** - Database migrations
- **OpenAPI/Swagger** - API documentation
- **Docker** - Containerization

## Features

- Complete RESTful API for inventory management
- JWT authentication with Spring Security
- Automatic documentation with OpenAPI
- Report generation (PDF, Excel)
- Redis caching
- Automatic database migrations
- Monitoring with Spring Actuator

## Requirements

- Java 21+
- PostgreSQL 13+
- Redis 6+
- Gradle 8+

## Installation and Execution

### Local Development

1. Clone the repository:
```bash
git clone <repository-url>
cd KinvexCore
```

2. Configure environment variables (copy `.env.example` to `.env`):
```bash
cp .env.example .env
```

3. Run with Gradle:
```bash
./gradlew bootRun
```

### Docker

```bash
docker build -t kinvex-core .
docker run -p 8080:8080 kinvex-core
```

## API Documentation

Once running, API documentation will be available at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Project Structure

```
src/
├── main/
│   ├── java/dev/kreaker/kinvex/
│   │   ├── config/          # Configurations
│   │   ├── controller/      # REST Controllers
│   │   ├── service/         # Business logic
│   │   ├── repository/      # JPA Repositories
│   │   ├── entity/          # JPA Entities
│   │   ├── dto/             # DTOs
│   │   └── security/        # Security configuration
│   └── resources/
│       ├── application.yml  # Main configuration
│       └── db/migration/    # Flyway migrations
└── test/                    # Unit and integration tests
```

## Testing

```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport
```

## Contributing

1. Fork the project
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -am 'Add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Create a Pull Request

## License

This project is under the MIT license. See the [LICENSE](LICENSE) file for more details.

**Developed by [Kreaker.dev](https://kreaker.dev)**
