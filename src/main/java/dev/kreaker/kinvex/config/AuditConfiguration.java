package dev.kreaker.kinvex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuración para habilitar el sistema de auditoría automática.
 *
 * <p>Habilita AspectJ para permitir que los interceptores de auditoría funcionen correctamente.
 */
@Configuration
@EnableAspectJAutoProxy
public class AuditConfiguration {
    // Esta clase solo necesita las anotaciones para habilitar AOP
}
