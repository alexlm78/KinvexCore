package dev.kreaker.kinvex.service;

import dev.kreaker.kinvex.config.LoggingConfiguration;
import dev.kreaker.kinvex.config.MetricsConfiguration;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * Servicio de monitoreo que integra métricas y logging para el sistema Kinvex. Proporciona métodos
 * convenientes para registrar operaciones y métricas.
 */
@Service
@ConditionalOnBean(MetricsConfiguration.class)
public class MonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringService.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("dev.kreaker.kinvex.audit");
    private static final Logger securityLogger =
            LoggerFactory.getLogger("dev.kreaker.kinvex.security");
    private static final Logger performanceLogger =
            LoggerFactory.getLogger("dev.kreaker.kinvex.performance");

    private final MetricsConfiguration metricsConfiguration;
    private final Counter inventoryOperationsCounter;
    private final Counter stockMovementsCounter;
    private final Counter orderOperationsCounter;
    private final Counter authenticationAttemptsCounter;
    private final Counter apiRequestsCounter;
    private final Timer inventoryOperationTimer;
    private final Timer orderProcessingTimer;
    private final Timer reportGenerationTimer;

    @Autowired
    public MonitoringService(
            MetricsConfiguration metricsConfiguration,
            Counter inventoryOperationsCounter,
            Counter stockMovementsCounter,
            Counter orderOperationsCounter,
            Counter authenticationAttemptsCounter,
            Counter apiRequestsCounter,
            Timer inventoryOperationTimer,
            Timer orderProcessingTimer,
            Timer reportGenerationTimer) {
        this.metricsConfiguration = metricsConfiguration;
        this.inventoryOperationsCounter = inventoryOperationsCounter;
        this.stockMovementsCounter = stockMovementsCounter;
        this.orderOperationsCounter = orderOperationsCounter;
        this.authenticationAttemptsCounter = authenticationAttemptsCounter;
        this.apiRequestsCounter = apiRequestsCounter;
        this.inventoryOperationTimer = inventoryOperationTimer;
        this.orderProcessingTimer = orderProcessingTimer;
        this.reportGenerationTimer = reportGenerationTimer;
    }

    /** Registra una operación de inventario con métricas y logging. */
    public <T> T recordInventoryOperation(
            String operation, String productCode, Integer quantity, Supplier<T> operationSupplier) {
        Instant start = Instant.now();
        String result = "SUCCESS";

        try {
            LoggingConfiguration.LoggingUtils.setOperationContext(
                    operation, "PRODUCT", productCode);
            T operationResult =
                    inventoryOperationTimer.recordCallable(() -> operationSupplier.get());

            inventoryOperationsCounter.increment();
            if (quantity != null
                    && (operation.contains("DECREASE") || operation.contains("INCREASE"))) {
                stockMovementsCounter.increment();
            }

            return operationResult;
        } catch (Exception e) {
            result = "ERROR";
            LoggingConfiguration.LoggingUtils.logError(
                    logger, operation, e, "productCode=" + productCode + ", quantity=" + quantity);
            throw new RuntimeException(e);
        } finally {
            long duration = Duration.between(start, Instant.now()).toMillis();
            LoggingConfiguration.LoggingUtils.logInventoryOperation(
                    logger, operation, productCode, quantity, result);
            LoggingConfiguration.LoggingUtils.logPerformance(
                    performanceLogger, operation, duration, "productCode=" + productCode);
        }
    }

    /** Registra una operación de orden con métricas y logging. */
    public <T> T recordOrderOperation(
            String operation, String orderNumber, String status, Supplier<T> operationSupplier) {
        Instant start = Instant.now();
        String result = "SUCCESS";

        try {
            LoggingConfiguration.LoggingUtils.setOperationContext(operation, "ORDER", orderNumber);
            T operationResult = orderProcessingTimer.recordCallable(() -> operationSupplier.get());

            orderOperationsCounter.increment();

            return operationResult;
        } catch (Exception e) {
            result = "ERROR";
            LoggingConfiguration.LoggingUtils.logError(
                    logger, operation, e, "orderNumber=" + orderNumber + ", status=" + status);
            throw new RuntimeException(e);
        } finally {
            long duration = Duration.between(start, Instant.now()).toMillis();
            LoggingConfiguration.LoggingUtils.logOrderOperation(
                    logger, operation, orderNumber, status, result);
            LoggingConfiguration.LoggingUtils.logPerformance(
                    performanceLogger, operation, duration, "orderNumber=" + orderNumber);
        }
    }

    /** Registra una operación de generación de reportes. */
    public <T> T recordReportGeneration(
            String reportType, String parameters, Supplier<T> operationSupplier) {
        Instant start = Instant.now();
        String result = "SUCCESS";

        try {
            LoggingConfiguration.LoggingUtils.setOperationContext(
                    "GENERATE_REPORT", "REPORT", reportType);
            T operationResult = reportGenerationTimer.recordCallable(() -> operationSupplier.get());

            return operationResult;
        } catch (Exception e) {
            result = "ERROR";
            LoggingConfiguration.LoggingUtils.logError(
                    logger,
                    "GENERATE_REPORT",
                    e,
                    "reportType=" + reportType + ", parameters=" + parameters);
            throw new RuntimeException(e);
        } finally {
            long duration = Duration.between(start, Instant.now()).toMillis();
            logger.info(
                    "REPORT_GENERATION: type={}, parameters={}, result={}, duration={}ms",
                    reportType,
                    parameters,
                    result,
                    duration);
            LoggingConfiguration.LoggingUtils.logPerformance(
                    performanceLogger, "GENERATE_REPORT", duration, "reportType=" + reportType);
        }
    }

    /** Registra un intento de autenticación. */
    public void recordAuthenticationAttempt(
            String operation, String username, boolean success, String ipAddress) {
        authenticationAttemptsCounter.increment();

        String result = success ? "SUCCESS" : "FAILURE";
        LoggingConfiguration.LoggingUtils.logAuthOperation(
                securityLogger, operation, username, result, ipAddress);

        if (!success) {
            logger.warn(
                    "AUTHENTICATION_FAILURE: operation={}, username={}, ipAddress={}",
                    operation,
                    username,
                    ipAddress);
        }
    }

    /** Registra una operación de auditoría. */
    public void recordAuditOperation(
            String action, String entityType, String entityId, String userId) {
        LoggingConfiguration.LoggingUtils.logAuditOperation(
                auditLogger, action, entityType, entityId, userId);
    }

    /** Registra una request de API. */
    public void recordApiRequest(String method, String endpoint, int statusCode, long durationMs) {
        apiRequestsCounter.increment();

        logger.info(
                "API_REQUEST: method={}, endpoint={}, statusCode={}, duration={}ms",
                method,
                endpoint,
                statusCode,
                durationMs);

        if (durationMs > 2000) {
            performanceLogger.warn(
                    "SLOW_API_REQUEST: method={}, endpoint={}, duration={}ms",
                    method,
                    endpoint,
                    durationMs);
        }
    }

    /** Actualiza métricas de sistema. */
    public void updateSystemMetrics(int totalProducts, int lowStockProducts, int activeUsers) {
        metricsConfiguration.updateTotalProducts(totalProducts);
        metricsConfiguration.updateLowStockProducts(lowStockProducts);

        logger.debug(
                "SYSTEM_METRICS_UPDATE: totalProducts={}, lowStockProducts={}, activeUsers={}",
                totalProducts,
                lowStockProducts,
                activeUsers);
    }

    /** Registra el login de un usuario. */
    public void recordUserLogin(String username) {
        metricsConfiguration.incrementActiveUsers();
        logger.info("USER_LOGIN: username={}", username);
    }

    /** Registra el logout de un usuario. */
    public void recordUserLogout(String username) {
        metricsConfiguration.decrementActiveUsers();
        logger.info("USER_LOGOUT: username={}", username);
    }

    /** Registra un error crítico del sistema. */
    public void recordCriticalError(String component, String operation, Exception exception) {
        logger.error(
                "CRITICAL_ERROR: component={}, operation={}, error={}",
                component,
                operation,
                exception.getMessage(),
                exception);

        // Aquí se podría integrar con sistemas de alertas externos
        // como PagerDuty, Slack, etc.
    }

    /** Registra métricas de salud del sistema. */
    public void recordHealthMetrics(String component, String status, String details) {
        logger.info(
                "HEALTH_CHECK: component={}, status={}, details={}", component, status, details);
    }
}
