package dev.kreaker.kinvex.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createProduct_WithValidData_ShouldPass() {
        // Given
        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        product.setDescription("Test description");
        product.setCurrentStock(10);
        product.setMinStock(5);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertThat(violations).isEmpty();
        assertThat(product.getCode()).isEqualTo("PROD001");
        assertThat(product.getName()).isEqualTo("Test Product");
        assertThat(product.getUnitPrice()).isEqualTo(new BigDecimal("99.99"));
        assertThat(product.getActive()).isTrue();
        assertThat(product.getCurrentStock()).isEqualTo(10);
        assertThat(product.getMinStock()).isEqualTo(5);
    }

    @Test
    void createProduct_WithBlankCode_ShouldFailValidation() {
        // Given
        Product product = new Product("", "Test Product", new BigDecimal("99.99"));

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("must not be blank");
    }

    @Test
    void createProduct_WithBlankName_ShouldFailValidation() {
        // Given
        Product product = new Product("PROD001", "", new BigDecimal("99.99"));

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("must not be blank");
    }

    @Test
    void createProduct_WithNullUnitPrice_ShouldFailValidation() {
        // Given
        Product product = new Product("PROD001", "Test Product", null);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("must not be null");
    }

    @Test
    void createProduct_WithZeroUnitPrice_ShouldFailValidation() {
        // Given
        Product product = new Product("PROD001", "Test Product", BigDecimal.ZERO);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("must be greater than 0");
    }

    @Test
    void createProduct_WithNegativeStock_ShouldFailValidation() {
        // Given
        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        product.setCurrentStock(-1);

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must be greater than or equal to 0");
    }

    @Test
    void isLowStock_WhenCurrentStockEqualsMinStock_ShouldReturnTrue() {
        // Given
        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        product.setCurrentStock(5);
        product.setMinStock(5);

        // When & Then
        assertThat(product.isLowStock()).isTrue();
    }

    @Test
    void isLowStock_WhenCurrentStockBelowMinStock_ShouldReturnTrue() {
        // Given
        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        product.setCurrentStock(3);
        product.setMinStock(5);

        // When & Then
        assertThat(product.isLowStock()).isTrue();
    }

    @Test
    void isLowStock_WhenCurrentStockAboveMinStock_ShouldReturnFalse() {
        // Given
        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        product.setCurrentStock(10);
        product.setMinStock(5);

        // When & Then
        assertThat(product.isLowStock()).isFalse();
    }

    @Test
    void isOverStock_WhenCurrentStockAboveMaxStock_ShouldReturnTrue() {
        // Given
        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        product.setCurrentStock(15);
        product.setMaxStock(10);

        // When & Then
        assertThat(product.isOverStock()).isTrue();
    }

    @Test
    void isOverStock_WhenMaxStockIsNull_ShouldReturnFalse() {
        // Given
        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        product.setCurrentStock(100);
        product.setMaxStock(null);

        // When & Then
        assertThat(product.isOverStock()).isFalse();
    }

    @Test
    void increaseStock_WithValidQuantity_ShouldIncreaseStock() {
        // Given
        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        product.setCurrentStock(10);

        // When
        product.increaseStock(5);

        // Then
        assertThat(product.getCurrentStock()).isEqualTo(15);
    }

    @Test
    void increaseStock_WithZeroQuantity_ShouldNotChangeStock() {
        // Given
        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        product.setCurrentStock(10);

        // When
        product.increaseStock(0);

        // Then
        assertThat(product.getCurrentStock()).isEqualTo(10);
    }

    @Test
    void decreaseStock_WithValidQuantity_ShouldDecreaseStock() {
        // Given
        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        product.setCurrentStock(10);

        // When
        product.decreaseStock(3);

        // Then
        assertThat(product.getCurrentStock()).isEqualTo(7);
    }

    @Test
    void decreaseStock_WithInsufficientStock_ShouldThrowException() {
        // Given
        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        product.setCurrentStock(5);

        // When & Then
        assertThatThrownBy(() -> product.decreaseStock(10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Insufficient stock or invalid quantity");
    }

    @Test
    void decreaseStock_WithZeroQuantity_ShouldThrowException() {
        // Given
        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        product.setCurrentStock(10);

        // When & Then
        assertThatThrownBy(() -> product.decreaseStock(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Insufficient stock or invalid quantity");
    }

    @Test
    void hasAvailableStock_WithSufficientStock_ShouldReturnTrue() {
        // Given
        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        product.setCurrentStock(10);

        // When & Then
        assertThat(product.hasAvailableStock(5)).isTrue();
        assertThat(product.hasAvailableStock(10)).isTrue();
    }

    @Test
    void hasAvailableStock_WithInsufficientStock_ShouldReturnFalse() {
        // Given
        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        product.setCurrentStock(5);

        // When & Then
        assertThat(product.hasAvailableStock(10)).isFalse();
    }
}
