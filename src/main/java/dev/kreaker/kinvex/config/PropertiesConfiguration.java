package dev.kreaker.kinvex.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración que habilita las propiedades personalizadas de la aplicación.
 * Esta clase registra todas las clases de propiedades para que Spring Boot las
 * procese automáticamente.
 */
@Configuration
@EnableConfigurationProperties({
    AppProperties.class,
    JwtProperties.class,
    CorsProperties.class
})
public class PropertiesConfiguration {
    // Esta clase solo sirve para habilitar las propiedades de configuración
    // No necesita métodos adicionales
}
