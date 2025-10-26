package dev.kreaker.kinvex.config;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test para verificar la configuración básica de base de datos.
 */
@SpringBootTest
@ActiveProfiles("test")
class DatabaseConfigTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDataSourceConfiguration() {
        assertThat(dataSource).isNotNull();
        assertThat(dataSource.getClass().getSimpleName()).contains("Hikari");
    }

    @Test
    void testApplicationContextLoads() {
        // Este test verifica que el contexto de Spring se carga correctamente
        // con la configuración de base de datos H2 para testing
        assertThat(dataSource).isNotNull();
    }
}
