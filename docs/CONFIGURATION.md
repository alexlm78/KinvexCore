# Configuración de KinvexCore

## Perfiles de Aplicación

KinvexCore soporta múltiples perfiles de configuración para diferentes entornos:

### Perfiles Disponibles

- **`dev`** - Desarrollo local
- **`test`** - Ejecución de pruebas
- **`staging`** - Ambiente de pre-producción
- **`prod`** - Producción

### Activación de Perfiles

```bash
# Variable de entorno
export SPRING_PROFILES_ACTIVE=dev

# Argumento JVM
java -Dspring.profiles.active=prod -jar kinvex-core.jar

# Propiedad en application.yml
spring:
  profiles:
    active: dev
```

## Configuración por Ambiente

### Desarrollo (`dev`)

- Base de datos PostgreSQL local
- Logs detallados habilitados
- Endpoints de desarrollo disponibles
- JWT con expiración extendida (2 horas)
- CORS permisivo para desarrollo local

### Testing (`test`)

- Base de datos H2 en memoria
- Configuración optimizada para pruebas rápidas
- JWT con expiración corta (5 minutos)
- Logs mínimos

### Staging (`staging`)

- Configuración similar a producción
- Endpoints de prueba habilitados
- Logs moderados
- Validaciones de seguridad activas

### Producción (`prod`)

- Configuración de seguridad máxima
- Logs optimizados para rendimiento
- JWT con expiración corta (30 minutos)
- CORS restrictivo
- Métricas y monitoreo habilitados

## Variables de Entorno

### Variables Requeridas en Producción

```bash
# Base de datos
DB_HOST=your-db-host
DB_PORT=5432
DB_NAME=kinvex_prod
DB_USERNAME=kinvex_user
DB_PASSWORD=secure-password

# Redis
REDIS_HOST=your-redis-host
REDIS_PORT=6379
REDIS_PASSWORD=secure-redis-password

# JWT (CRÍTICO)
JWT_SECRET=your-super-secure-256-bit-secret-key

# CORS
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
```

### Variables Opcionales

```bash
# Servidor
SERVER_PORT=8080
SERVER_TOMCAT_MAX_THREADS=200

# Pool de conexiones
DB_POOL_SIZE=20
DB_POOL_MIN_IDLE=10

# Cache
CACHE_TTL=7200000

# Logging
LOG_FILE=/var/log/kinvex/kinvex-core.log
LOG_MAX_FILE_SIZE=100MB
```

## Configuración de Seguridad

### JWT

- **Desarrollo**: Clave simple, expiración larga
- **Producción**: Clave segura (mínimo 256 bits), expiración corta

### CORS

- **Desarrollo**: Orígenes múltiples permitidos
- **Producción**: Solo dominios específicos

### Base de Datos

- **Desarrollo**: Pool pequeño, DDL automático
- **Producción**: Pool optimizado, solo validación

## Monitoreo y Métricas

### Endpoints Actuator

```bash
# Health check
GET /actuator/health

# Métricas Prometheus
GET /actuator/prometheus

# Información de la aplicación
GET /actuator/info
```

### Configuración de Logs

Los logs se configuran automáticamente según el perfil:

- **Desarrollo**: Consola + archivo, nivel DEBUG
- **Testing**: Solo consola, nivel WARN
- **Producción**: Archivo rotativo, nivel INFO

## Validación de Configuración

La aplicación incluye validaciones automáticas:

1. **JWT Secret**: Valida longitud y contenido en producción
2. **Base de datos**: Verifica conectividad al inicio
3. **Redis**: Valida conexión de cache
4. **CORS**: Verifica configuración de orígenes

## Ejemplos de Configuración

### Docker Compose (Desarrollo)

```yaml
version: "3.8"
services:
  kinvex-core:
    image: kinvex-core:latest
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - DB_HOST=postgres
      - REDIS_HOST=redis
    depends_on:
      - postgres
      - redis
```

### Kubernetes (Producción)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kinvex-core
spec:
  template:
    spec:
      containers:
        - name: kinvex-core
          image: kinvex-core:1.0.0
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "prod"
            - name: JWT_SECRET
              valueFrom:
                secretKeyRef:
                  name: kinvex-secrets
                  key: jwt-secret
```

## Troubleshooting

### Problemas Comunes

1. **JWT Secret inválido**

   ```
   Error: JWT secret debe tener al menos 32 caracteres
   Solución: Configurar JWT_SECRET con valor seguro
   ```

2. **Conexión a base de datos fallida**

   ```
   Error: Connection refused
   Solución: Verificar DB_HOST, DB_PORT y credenciales
   ```

3. **CORS bloqueado**
   ```
   Error: CORS policy blocked
   Solución: Agregar origen a CORS_ALLOWED_ORIGINS
   ```

### Logs de Diagnóstico

```bash
# Habilitar logs de configuración
export LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_BOOT_CONTEXT_CONFIG=DEBUG

# Ver propiedades cargadas
java -Dlogging.level.org.springframework.core.env=DEBUG -jar kinvex-core.jar
```
