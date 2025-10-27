package dev.kreaker.kinvex.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Configuración de logging para el sistema Kinvex. Proporciona utilidades para logging estructurado
 * y trazabilidad.
 */
@Configuration
public class LoggingConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfiguration.class);

    /** Filtro para logging de requests HTTP. */
    @Bean
    @ConditionalOnMissingBean(name = "requestLoggingFilter")
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeClientInfo(true);
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(1000);
        filter.setIncludeHeaders(false);
        filter.setBeforeMessagePrefix("REQUEST: ");
        filter.setAfterMessagePrefix("RESPONSE: ");
        return filter;
    }

    /** Utilidades para logging estructurado. */
    public static class LoggingUtils {

        private static final String CORRELATION_ID_KEY = "correlationId";
        private static final String USER_ID_KEY = "userId";
        private static final String OPERATION_KEY = "operation";
        private static final String ENTITY_TYPE_KEY = "entityType";
        private static final String ENTITY_ID_KEY = "entityId";

        /** Establece un ID de correlación para trazabilidad de requests. */
        public static void setCorrelationId(String correlationId) {
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            MDC.put(CORRELATION_ID_KEY, correlationId);
        }

        /** Establece el ID del usuario para contexto de logging. */
        public static void setUserId(String userId) {
            if (userId != null) {
                MDC.put(USER_ID_KEY, userId);
            }
        }

        /** Establece el contexto de operación para logging. */
        public static void setOperationContext(
                String operation, String entityType, String entityId) {
            if (operation != null) {
                MDC.put(OPERATION_KEY, operation);
            }
            if (entityType != null) {
                MDC.put(ENTITY_TYPE_KEY, entityType);
            }
            if (entityId != null) {
                MDC.put(ENTITY_ID_KEY, entityId);
            }
        }

        /** Limpia el contexto de logging. */
        public static void clearContext() {
            MDC.clear();
        }

        /** Obtiene el ID de correlación actual. */
        public static String getCorrelationId() {
            return MDC.get(CORRELATION_ID_KEY);
        }

        /** Logging estructurado para operaciones de inventario. */
        public static void logInventoryOperation(
                Logger logger,
                String operation,
                String productCode,
                Integer quantity,
                String result) {
            logger.info(
                    "INVENTORY_OPERATION: operation={}, productCode={}, quantity={}, result={}",
                    operation,
                    productCode,
                    quantity,
                    result);
        }

        /** Logging estructurado para operaciones de órdenes. */
        public static void logOrderOperation(
                Logger logger, String operation, String orderNumber, String status, String result) {
            logger.info(
                    "ORDER_OPERATION: operation={}, orderNumber={}, status={}, result={}",
                    operation,
                    orderNumber,
                    status,
                    result);
        }

        /** Logging estructurado para operaciones de autenticación. */
        public static void logAuthOperation(
                Logger logger, String operation, String username, String result, String ipAddress) {
            logger.info(
                    "AUTH_OPERATION: operation={}, username={}, result={}, ipAddress={}",
                    operation,
                    username,
                    result,
                    ipAddress);
        }

        /** Logging estructurado para operaciones de auditoría. */
        public static void logAuditOperation(
                Logger logger, String action, String entityType, String entityId, String userId) {
            logger.info(
                    "AUDIT_OPERATION: action={}, entityType={}, entityId={}, userId={}",
                    action,
                    entityType,
                    entityId,
                    userId);
        }

        /** Logging de performance para operaciones críticas. */
        public static void logPerformance(
                Logger logger, String operation, long durationMs, String additionalInfo) {
            if (durationMs > 1000) { // Log slow operations
                logger.warn(
                        "SLOW_OPERATION: operation={}, duration={}ms, info={}",
                        operation,
                        durationMs,
                        additionalInfo);
            } else {
                logger.debug(
                        "PERFORMANCE: operation={}, duration={}ms, info={}",
                        operation,
                        durationMs,
                        additionalInfo);
            }
        }

        /** Logging de errores con contexto completo. */
        public static void logError(
                Logger logger, String operation, Exception exception, String additionalContext) {
            logger.error(
                    "ERROR: operation={}, exception={}, message={}, context={}",
                    operation,
                    exception.getClass().getSimpleName(),
                    exception.getMessage(),
                    additionalContext,
                    exception);
        }
    }

    /** Interceptor para agregar contexto de logging automáticamente. */
    public static class RequestContextInterceptor {

        public static void setupRequestContext(HttpServletRequest request, String userId) {
            // Establecer ID de correlación
            String correlationId = request.getHeader("X-Correlation-ID");
            LoggingUtils.setCorrelationId(correlationId);

            // Establecer contexto de usuario
            LoggingUtils.setUserId(userId);

            // Log del inicio de request
            logger.debug(
                    "REQUEST_START: method={}, uri={}, correlationId={}, userId={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    LoggingUtils.getCorrelationId(),
                    userId);
        }

        public static void cleanupRequestContext() {
            logger.debug("REQUEST_END: correlationId={}", LoggingUtils.getCorrelationId());
            LoggingUtils.clearContext();
        }
    }
}
