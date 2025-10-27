package dev.kreaker.kinvex.config;

import dev.kreaker.kinvex.service.MonitoringService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Filtro para logging de requests HTTP y establecimiento de contexto de trazabilidad. */
@Component
@Order(1)
@ConditionalOnBean(MonitoringService.class)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_RESPONSE_HEADER = "X-Correlation-ID";

    private final MonitoringService monitoringService;

    @Autowired
    public RequestLoggingFilter(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip logging for actuator endpoints to avoid noise
        if (request.getRequestURI().startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        Instant startTime = Instant.now();

        // Establecer o generar correlation ID
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Agregar correlation ID a la respuesta
        response.setHeader(CORRELATION_ID_RESPONSE_HEADER, correlationId);

        // Establecer contexto de logging
        LoggingConfiguration.LoggingUtils.setCorrelationId(correlationId);

        // Obtener información del usuario si está disponible
        String userId = extractUserId(request);
        if (userId != null) {
            LoggingConfiguration.LoggingUtils.setUserId(userId);
        }

        // Log del inicio del request
        logger.info(
                "REQUEST_START: method={}, uri={}, correlationId={}, userId={}, userAgent={}, remoteAddr={}",
                request.getMethod(),
                request.getRequestURI(),
                correlationId,
                userId,
                request.getHeader("User-Agent"),
                getClientIpAddress(request));

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Calcular duración
            long durationMs = Instant.now().toEpochMilli() - startTime.toEpochMilli();

            // Log del final del request
            logger.info(
                    "REQUEST_END: method={}, uri={}, status={}, duration={}ms, correlationId={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs,
                    correlationId);

            // Registrar métricas
            monitoringService.recordApiRequest(
                    request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs);

            // Limpiar contexto
            LoggingConfiguration.LoggingUtils.clearContext();
        }
    }

    /** Extrae el ID del usuario del request (desde JWT token, sesión, etc.) */
    private String extractUserId(HttpServletRequest request) {
        // Aquí se podría extraer el usuario del JWT token o sesión
        // Por ahora retornamos null, se implementará cuando esté el sistema de auth
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // TODO: Extraer userId del JWT token
            return "user_from_jwt";
        }
        return null;
    }

    /** Obtiene la dirección IP real del cliente considerando proxies. */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
