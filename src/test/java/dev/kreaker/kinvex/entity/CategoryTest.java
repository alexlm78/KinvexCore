package dev.kreaker.kinvex.entity;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CategoryTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createCategory_WithValidData_ShouldPass() {
        // Given
        Category category = new Category("Electronics", "Electronic products and devices");

        // When
        Set<ConstraintViolation<Category>> violations = validator.validate(category);

        // Then
        assertThat(violations).isEmpty();
        assertThat(category.getName()).isEqualTo("Electronics");
        assertThat(category.getDescription()).isEqualTo("Electronic products and devices");
    }

    @Test
    void createCategory_WithBlankName_ShouldFailValidation() {
        // Given
        Category category = new Category("", "Some description");

        // When
        Set<ConstraintViolation<Category>> violations = validator.validate(category);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("must not be blank");
    }

    @Test
    void createCategory_WithTooLongName_ShouldFailValidation() {
        // Given
        String longName = "a".repeat(51); // Max is 50
        Category category = new Category(longName);

        // When
        Set<ConstraintViolation<Category>> violations = validator.validate(category);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("size must be between 0 and 50");
    }

    @Test
    void createCategory_WithTooLongDescription_ShouldFailValidation() {
        // Given
        String longDescription = "a".repeat(501); // Max is 500
        Category category = new Category("Electronics", longDescription);

        // When
        Set<ConstraintViolation<Category>> violations = validator.validate(category);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("size must be between 0 and 500");
    }

    @Test
    void addChild_ShouldAddChildAndSetParent() {
        // Given
        Category parent = new Category("Electronics");
        Category child = new Category("Smartphones");

        // When
        parent.addChild(child);

        // Then
        assertThat(parent.getChildren()).hasSize(1);
        assertThat(parent.getChildren().get(0)).isEqualTo(child);
        assertThat(child.getParent()).isEqualTo(parent);
    }

    @Test
    void removeChild_ShouldRemoveChildAndClearParent() {
        // Given
        Category parent = new Category("Electronics");
        Category child = new Category("Smartphones");
        parent.addChild(child);

        // When
        parent.removeChild(child);

        // Then
        assertThat(parent.getChildren()).isEmpty();
        assertThat(child.getParent()).isNull();
    }

    @Test
    void addProduct_ShouldAddProductAndSetCategory() {
        // Given
        Category category = new Category("Electronics");
        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));

        // When
        category.addProduct(product);

        // Then
        assertThat(category.getProducts()).hasSize(1);
        assertThat(category.getProducts().get(0)).isEqualTo(product);
        assertThat(product.getCategory()).isEqualTo(category);
    }

    @Test
    void removeProduct_ShouldRemoveProductAndClearCategory() {
        // Given
        Category category = new Category("Electronics");
        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        category.addProduct(product);

        // When
        category.removeProduct(product);

        // Then
        assertThat(category.getProducts()).isEmpty();
        assertThat(product.getCategory()).isNull();
    }

    @Test
    void hierarchicalCategories_ShouldWorkCorrectly() {
        // Given
        Category electronics = new Category("Electronics");
        Category smartphones = new Category("Smartphones");
        Category tablets = new Category("Tablets");
        Category iphones = new Category("iPhones");

        // When
        electronics.addChild(smartphones);
        electronics.addChild(tablets);
        smartphones.addChild(iphones);

        // Then
        assertThat(electronics.getChildren()).hasSize(2);
        assertThat(electronics.getChildren()).contains(smartphones, tablets);
        assertThat(smartphones.getParent()).isEqualTo(electronics);
        assertThat(tablets.getParent()).isEqualTo(electronics);
        assertThat(smartphones.getChildren()).hasSize(1);
        assertThat(smartphones.getChildren().get(0)).isEqualTo(iphones);
        assertThat(iphones.getParent()).isEqualTo(smartphones);
    }

    @Test
    void defaultConstructor_ShouldInitializeCollections() {
        // Given & When
        Category category = new Category();

        // Then
        assertThat(category.getChildren()).isNotNull();
        assertThat(category.getChildren()).isEmpty();
        assertThat(category.getProducts()).isNotNull();
        assertThat(category.getProducts()).isEmpty();
    }
}
