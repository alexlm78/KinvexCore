package dev.kreaker.kinvex.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

/**
 * Propiedades de configuración de la aplicación Kinvex. Mapea las propiedades
 * personalizadas definidas en application.yml
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String name,
        String version,
        String description,
        Contact contact,
        boolean debug,
        boolean mockExternalServices,
        boolean enableTestEndpoints,
        boolean testMode,
        Security security,
        Monitoring monitoring
        ) {

    public record Contact(
            String name,
            String email
            ) {

    }

    public record Security(
            boolean requireSsl,
            int sessionTimeout
            ) {

    }

    public record Monitoring(
            boolean enabled,
            boolean metricsEnabled
            ) {

    }

    // Constructor por defecto para valores opcionales
    @ConstructorBinding
    public AppProperties(
            String name,
            String version,
            String description,
            Contact contact,
            Boolean debug,
            Boolean mockExternalServices,
            Boolean enableTestEndpoints,
            Boolean testMode,
            Security security,
            Monitoring monitoring
    ) {
        this(
                name != null ? name : "Kinvex Inventory System",
                version != null ? version : "1.0.0",
                description != null ? description : "Sistema de Gestión de Inventario Empresarial",
                contact != null ? contact : new Contact("Kinvex Team", "support@kinvex.com"),
                debug != null ? debug : false,
                mockExternalServices != null ? mockExternalServices : false,
                enableTestEndpoints != null ? enableTestEndpoints : false,
                testMode != null ? testMode : false,
                security != null ? security : new Security(false, 1800),
                monitoring != null ? monitoring : new Monitoring(true, true)
        );
    }
}
