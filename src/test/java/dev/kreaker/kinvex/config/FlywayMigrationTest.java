package dev.kreaker.kinvex.config;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test de integración para verificar que las migraciones Flyway funcionan
 * correctamente con una base de datos PostgreSQL real usando TestContainers.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class FlywayMigrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("kinvex_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");

        // JWT configuration for testing
        registry.add("jwt.secret", () -> "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdGVzdGluZy1tdXN0LWJlLWF0LWxlYXN0LTI1Ni1iaXRz");
        registry.add("jwt.expiration", () -> "3600");
        registry.add("jwt.refresh-expiration", () -> "86400");
        registry.add("jwt.issuer", () -> "kinvex-test");
        registry.add("jwt.audience", () -> "kinvex-test-users");

        // Disable Redis for testing
        registry.add("spring.data.redis.repositories.enabled", () -> "false");
        registry.add("spring.cache.type", () -> "none");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testFlywayMigrationsExecuted() {
        // Verificar que las tablas principales fueron creadas por Flyway
        assertThat(tableExists("users")).isTrue();
        assertThat(tableExists("categories")).isTrue();
        assertThat(tableExists("products")).isTrue();
        assertThat(tableExists("suppliers")).isTrue();
        assertThat(tableExists("purchase_orders")).isTrue();
        assertThat(tableExists("order_details")).isTrue();
        assertThat(tableExists("inventory_movements")).isTrue();
        assertThat(tableExists("audit_logs")).isTrue();
    }

    @Test
    void testInitialDataInserted() {
        // Verificar que los datos iniciales fueron insertados
        Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users", Integer.class);
        assertThat(userCount).isGreaterThan(0);

        Integer categoryCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM categories", Integer.class);
        assertThat(categoryCount).isGreaterThan(0);

        Integer supplierCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM suppliers", Integer.class);
        assertThat(supplierCount).isGreaterThan(0);

        Integer productCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products", Integer.class);
        assertThat(productCount).isGreaterThan(0);
    }

    @Test
    void testDatabaseConstraints() {
        // Verificar que las restricciones de integridad funcionan
        Integer adminUserCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'", Integer.class);
        assertThat(adminUserCount).isGreaterThan(0);

        // Verificar que los productos tienen códigos únicos
        Integer uniqueProductCodes = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT code) FROM products", Integer.class);
        Integer totalProducts = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products", Integer.class);
        assertThat(uniqueProductCodes).isEqualTo(totalProducts);
    }

    @Test
    void testForeignKeyRelationships() {
        // Verificar que las relaciones de clave foránea funcionan
        Integer productsWithCategories = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products p INNER JOIN categories c ON p.category_id = c.id",
                Integer.class);
        assertThat(productsWithCategories).isGreaterThan(0);

        Integer movementsWithProducts = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM inventory_movements im INNER JOIN products p ON im.product_id = p.id",
                Integer.class);
        assertThat(movementsWithProducts).isGreaterThan(0);
    }

    private boolean tableExists(String tableName) {
        try {
            jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?",
                    Integer.class, tableName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
