package dev.kreaker.kinvex.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Test para verificar la configuración de logging y monitoreo. */
class LoggingAndMonitoringConfigTest {

    @Test
    void testLoggingConfigurationCanBeInstantiated() {
        // Este test verifica que la configuración de logging puede ser instanciada
        LoggingConfiguration config = new LoggingConfiguration();
        assertThat(config).isNotNull();
    }

    @Test
    void testMetricsConfigurationCanBeInstantiated() {
        // Este test verifica que la configuración de métricas puede ser instanciada
        MetricsConfiguration config = new MetricsConfiguration();
        assertThat(config).isNotNull();
    }

    @Test
    void testLoggingUtilitiesWork() {
        // Test de las utilidades de logging
        LoggingConfiguration.LoggingUtils.setCorrelationId("test-123");
        String correlationId = LoggingConfiguration.LoggingUtils.getCorrelationId();
        assertThat(correlationId).isEqualTo("test-123");

        LoggingConfiguration.LoggingUtils.clearContext();
    }
}
