package dev.kreaker.kinvex.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de métricas personalizadas para el sistema Kinvex. Proporciona métricas específicas
 * del dominio de inventario.
 */
@Configuration
public class MetricsConfiguration {

    private final AtomicInteger activeUsers = new AtomicInteger(0);
    private final AtomicInteger totalProducts = new AtomicInteger(0);
    private final AtomicInteger lowStockProducts = new AtomicInteger(0);

    /** Configura métricas personalizadas para el sistema de inventario. */
    @Bean
    @ConditionalOnMissingBean(name = "inventoryOperationsCounter")
    public Counter inventoryOperationsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("kinvex.inventory.operations.total")
                .description("Total number of inventory operations")
                .tag("type", "all")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "stockMovementsCounter")
    public Counter stockMovementsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("kinvex.stock.movements.total")
                .description("Total number of stock movements")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "orderOperationsCounter")
    public Counter orderOperationsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("kinvex.orders.operations.total")
                .description("Total number of order operations")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "authenticationAttemptsCounter")
    public Counter authenticationAttemptsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("kinvex.auth.attempts.total")
                .description("Total number of authentication attempts")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "apiRequestsCounter")
    public Counter apiRequestsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("kinvex.api.requests.total")
                .description("Total number of API requests")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "inventoryOperationTimer")
    public Timer inventoryOperationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("kinvex.inventory.operations.duration")
                .description("Duration of inventory operations")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "orderProcessingTimer")
    public Timer orderProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("kinvex.orders.processing.duration")
                .description("Duration of order processing operations")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "reportGenerationTimer")
    public Timer reportGenerationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("kinvex.reports.generation.duration")
                .description("Duration of report generation")
                .register(meterRegistry);
    }

    /** Gauge para usuarios activos en el sistema. */
    @Bean
    @ConditionalOnMissingBean(name = "activeUsersGauge")
    public Gauge activeUsersGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("kinvex.users.active", activeUsers, AtomicInteger::get)
                .description("Number of active users in the system")
                .register(meterRegistry);
    }

    /** Gauge para total de productos en inventario. */
    @Bean
    @ConditionalOnMissingBean(name = "totalProductsGauge")
    public Gauge totalProductsGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("kinvex.products.total", totalProducts, AtomicInteger::get)
                .description("Total number of products in inventory")
                .register(meterRegistry);
    }

    /** Gauge para productos con stock bajo. */
    @Bean
    @ConditionalOnMissingBean(name = "lowStockProductsGauge")
    public Gauge lowStockProductsGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("kinvex.products.low_stock", lowStockProducts, AtomicInteger::get)
                .description("Number of products with low stock")
                .register(meterRegistry);
    }

    /** Contribuidor de información personalizada para el endpoint /actuator/info. */
    @Bean
    @ConditionalOnMissingBean(name = "customInfoContributor")
    public InfoContributor customInfoContributor() {
        return new InfoContributor() {
            @Override
            public void contribute(Info.Builder builder) {
                Map<String, Object> kinvexInfo = new HashMap<>();
                kinvexInfo.put("name", "Kinvex Inventory Management System");
                kinvexInfo.put("description", "Sistema empresarial de gestión de inventario");
                kinvexInfo.put("startup-time", LocalDateTime.now().toString());
                kinvexInfo.put(
                        "features",
                        Map.of(
                                "inventory-management", "enabled",
                                "order-processing", "enabled",
                                "reporting", "enabled",
                                "audit-logging", "enabled",
                                "external-api", "enabled"));

                Map<String, Object> metrics = new HashMap<>();
                metrics.put("active-users", activeUsers.get());
                metrics.put("total-products", totalProducts.get());
                metrics.put("low-stock-products", lowStockProducts.get());
                kinvexInfo.put("current-metrics", metrics);

                builder.withDetail("kinvex", kinvexInfo);
            }
        };
    }

    // Métodos para actualizar las métricas desde otros componentes
    public void incrementActiveUsers() {
        activeUsers.incrementAndGet();
    }

    public void decrementActiveUsers() {
        activeUsers.decrementAndGet();
    }

    public void updateTotalProducts(int count) {
        totalProducts.set(count);
    }

    public void updateLowStockProducts(int count) {
        lowStockProducts.set(count);
    }
}
