# Kinvex Core - Backend API

Backend API del sistema de inventario Kinvex desarrollado con Spring Boot.

## Tecnologías

- **Java 21**
- **Spring Boot 3.2.0**
- **Spring Security** - Autenticación y autorización
- **Spring Data JPA** - Persistencia de datos
- **PostgreSQL** - Base de datos principal
- **Redis** - Cache y sesiones
- **JWT** - Tokens de autenticación
- **Flyway** - Migraciones de base de datos
- **OpenAPI/Swagger** - Documentación de API
- **Docker** - Containerización

## Características

- API RESTful completa para gestión de inventario
- Autenticación JWT con Spring Security
- Documentación automática con OpenAPI
- Generación de reportes (PDF, Excel)
- Cache con Redis
- Migraciones automáticas de base de datos
- Monitoreo con Spring Actuator

## Requisitos

- Java 21+
- PostgreSQL 13+
- Redis 6+
- Gradle 8+

## Instalación y Ejecución

### Desarrollo Local

1. Clona el repositorio:
```bash
git clone <repository-url>
cd KinvexCore
```

2. Configura las variables de entorno (copia `.env.example` a `.env`):
```bash
cp .env.example .env
```

3. Ejecuta con Gradle:
```bash
./gradlew bootRun
```

### Docker

```bash
docker build -t kinvex-core .
docker run -p 8080:8080 kinvex-core
```

## API Documentation

Una vez ejecutándose, la documentación de la API estará disponible en:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/dev/kreaker/kinvex/
│   │   ├── config/          # Configuraciones
│   │   ├── controller/      # Controladores REST
│   │   ├── service/         # Lógica de negocio
│   │   ├── repository/      # Repositorios JPA
│   │   ├── entity/          # Entidades JPA
│   │   ├── dto/             # DTOs
│   │   └── security/        # Configuración de seguridad
│   └── resources/
│       ├── application.yml  # Configuración principal
│       └── db/migration/    # Migraciones Flyway
└── test/                    # Tests unitarios e integración
```

## Testing

```bash
# Ejecutar todos los tests
./gradlew test

# Ejecutar tests con coverage
./gradlew test jacocoTestReport
```

## Contribución

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -am 'Añade nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crea un Pull Request

## Licencia

Este proyecto está bajo la licencia MIT. Ver el archivo [LICENSE](LICENSE) para más detalles.

**Desarrollado por [Kreaker.dev](https://kreaker.dev)**
