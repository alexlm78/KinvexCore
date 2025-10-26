package dev.kreaker.kinvex.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import dev.kreaker.kinvex.entity.Category;
import dev.kreaker.kinvex.entity.Product;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findByCode_ShouldReturnProduct_WhenProductExists() {
        // Given
        Category category = new Category("Electronics");
        entityManager.persistAndFlush(category);

        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        product.setCategory(category);
        product.setCurrentStock(10);
        entityManager.persistAndFlush(product);

        // When
        Optional<Product> found = productRepository.findByCode("PROD001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Product");
        assertThat(found.get().getUnitPrice()).isEqualTo(new BigDecimal("99.99"));
    }

    @Test
    void findLowStockProducts_ShouldReturnProductsWithLowStock() {
        // Given
        Category category = new Category("Electronics");
        entityManager.persistAndFlush(category);

        Product lowStockProduct = new Product("PROD001", "Low Stock Product", new BigDecimal("50.00"));
        lowStockProduct.setCategory(category);
        lowStockProduct.setCurrentStock(5);
        lowStockProduct.setMinStock(10);
        entityManager.persistAndFlush(lowStockProduct);

        Product normalStockProduct = new Product("PROD002", "Normal Stock Product", new BigDecimal("75.00"));
        normalStockProduct.setCategory(category);
        normalStockProduct.setCurrentStock(20);
        normalStockProduct.setMinStock(10);
        entityManager.persistAndFlush(normalStockProduct);

        // When
        List<Product> lowStockProducts = productRepository.findLowStockProducts();

        // Then
        assertThat(lowStockProducts).hasSize(1);
        assertThat(lowStockProducts.get(0).getCode()).isEqualTo("PROD001");
    }

    @Test
    void existsByCode_ShouldReturnTrue_WhenProductExists() {
        // Given
        Category category = new Category("Electronics");
        entityManager.persistAndFlush(category);

        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        product.setCategory(category);
        entityManager.persistAndFlush(product);

        // When
        boolean exists = productRepository.existsByCode("PROD001");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByCode_ShouldReturnFalse_WhenProductDoesNotExist() {
        // When
        boolean exists = productRepository.existsByCode("NONEXISTENT");

        // Then
        assertThat(exists).isFalse();
    }
}
