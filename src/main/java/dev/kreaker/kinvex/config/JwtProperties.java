package dev.kreaker.kinvex.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

/** Propiedades de configuración JWT. Mapea las propiedades JWT definidas en application.yml */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret, long expiration, long refreshExpiration, String issuer, String audience) {

    @ConstructorBinding
    public JwtProperties(
            String secret,
            Long expiration,
            Long refreshExpiration,
            String issuer,
            String audience) {
        this(
                secret != null ? secret : "default-secret-change-in-production",
                expiration != null ? expiration : 3600L,
                refreshExpiration != null ? refreshExpiration : 86400L,
                issuer != null ? issuer : "kinvex-system",
                audience != null ? audience : "kinvex-users");
    }

    /** Valida que la configuración JWT sea segura para producción */
    public void validateForProduction() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT secret debe tener al menos 32 caracteres para producción");
        }

        if (secret.contains("default") || secret.contains("change")) {
            throw new IllegalStateException(
                    "JWT secret no puede contener valores por defecto en producción");
        }

        if (expiration > 7200) { // 2 horas
            throw new IllegalStateException("JWT expiration no debe exceder 2 horas en producción");
        }
    }
}
