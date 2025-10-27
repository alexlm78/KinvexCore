package dev.kreaker.kinvex.config;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Indicador de salud personalizado para el sistema Kinvex. Verifica el estado de componentes
 * críticos del sistema.
 */
@Component
@ConditionalOnBean({JdbcTemplate.class, RedisTemplate.class})
public class CustomHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CustomHealthIndicator(
            JdbcTemplate jdbcTemplate, RedisTemplate<String, Object> redisTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        boolean isHealthy = true;

        // Verificar conexión a base de datos
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            details.put(
                    "database",
                    Map.of(
                            "status",
                            "UP",
                            "connection",
                            "OK",
                            "query_result",
                            result,
                            "timestamp",
                            LocalDateTime.now()));
        } catch (Exception e) {
            isHealthy = false;
            details.put(
                    "database",
                    Map.of(
                            "status", "DOWN",
                            "error", e.getMessage(),
                            "timestamp", LocalDateTime.now()));
        }

        // Verificar conexión a Redis
        try {
            redisTemplate.opsForValue().set("health:check", "OK");
            String redisResult = (String) redisTemplate.opsForValue().get("health:check");
            redisTemplate.delete("health:check");

            details.put(
                    "redis",
                    Map.of(
                            "status",
                            "UP",
                            "connection",
                            "OK",
                            "test_result",
                            redisResult,
                            "timestamp",
                            LocalDateTime.now()));
        } catch (Exception e) {
            isHealthy = false;
            details.put(
                    "redis",
                    Map.of(
                            "status", "DOWN",
                            "error", e.getMessage(),
                            "timestamp", LocalDateTime.now()));
        }

        // Verificar métricas del sistema
        try {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

            details.put(
                    "system",
                    Map.of(
                            "status",
                            memoryUsagePercent < 90 ? "UP" : "WARNING",
                            "memory_usage_percent",
                            String.format("%.2f%%", memoryUsagePercent),
                            "max_memory_mb",
                            maxMemory / 1024 / 1024,
                            "used_memory_mb",
                            usedMemory / 1024 / 1024,
                            "free_memory_mb",
                            freeMemory / 1024 / 1024,
                            "timestamp",
                            LocalDateTime.now()));

            if (memoryUsagePercent >= 95) {
                isHealthy = false;
            }
        } catch (Exception e) {
            details.put(
                    "system",
                    Map.of(
                            "status", "ERROR",
                            "error", e.getMessage(),
                            "timestamp", LocalDateTime.now()));
        }

        // Verificar estado de la aplicación
        details.put(
                "application",
                Map.of(
                        "name", "Kinvex Inventory System",
                        "status", "UP",
                        "startup_time", LocalDateTime.now(),
                        "version", "1.0.0"));

        if (isHealthy) {
            return Health.up().withDetails(details).build();
        } else {
            return Health.down().withDetails(details).build();
        }
    }
}
