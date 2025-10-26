package dev.kreaker.kinvex.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

/** Propiedades de configuración CORS. Mapea las propiedades CORS definidas en application.yml */
@ConfigurationProperties(prefix = "cors")
public record CorsProperties(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        boolean allowCredentials,
        long maxAge) {

    @ConstructorBinding
    public CorsProperties(
            List<String> allowedOrigins,
            List<String> allowedMethods,
            List<String> allowedHeaders,
            Boolean allowCredentials,
            Long maxAge) {
        this(
                allowedOrigins != null && !allowedOrigins.isEmpty()
                        ? allowedOrigins
                        : List.of("http://localhost:3000"),
                allowedMethods != null && !allowedMethods.isEmpty()
                        ? parseCommaSeparated(allowedMethods.get(0))
                        : List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"),
                allowedHeaders != null && !allowedHeaders.isEmpty() ? allowedHeaders : List.of("*"),
                allowCredentials != null ? allowCredentials : true,
                maxAge != null ? maxAge : 3600L);
    }

    /** Parsea una cadena separada por comas en una lista */
    private static List<String> parseCommaSeparated(String value) {
        if (value == null || value.trim().isEmpty()) {
            return List.of();
        }
        return List.of(value.split(","));
    }
}
