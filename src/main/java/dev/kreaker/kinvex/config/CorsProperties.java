package dev.kreaker.kinvex.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

/**
 * Propiedades de configuración CORS. Mapea las propiedades CORS definidas en
 * application.yml
 */
@ConfigurationProperties(prefix = "cors")
public record CorsProperties(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        boolean allowCredentials,
        long maxAge
        ) {

    @ConstructorBinding
    public CorsProperties(
            List<String> allowedOrigins,
            List<String> allowedMethods,
            List<String> allowedHeaders,
            Boolean allowCredentials,
            Long maxAge
    ) {
        this(
                allowedOrigins != null ? allowedOrigins : List.of("http://localhost:3000"),
                allowedMethods != null ? allowedMethods : List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"),
                allowedHeaders != null ? allowedHeaders : List.of("*"),
                allowCredentials != null ? allowCredentials : true,
                maxAge != null ? maxAge : 3600L
        );
    }
}
