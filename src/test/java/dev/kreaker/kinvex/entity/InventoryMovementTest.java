package dev.kreaker.kinvex.entity;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class InventoryMovementTest {

    private Validator validator;
    private Product testProduct;
    private User testUser;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        testProduct = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        testUser = new User("testuser", "test@example.com", "hashedpassword", User.UserRole.OPERATOR);
    }

    @Test
    void createInventoryMovement_WithValidData_ShouldPass() {
        // Given
        InventoryMovement movement = new InventoryMovement(testProduct, InventoryMovement.MovementType.IN, 10);
        movement.setCreatedBy(testUser);
        movement.setReferenceType(InventoryMovement.ReferenceType.PURCHASE_ORDER);
        movement.setReferenceId(1L);

        // When
        Set<ConstraintViolation<InventoryMovement>> violations = validator.validate(movement);

        // Then
        assertThat(violations).isEmpty();
        assertThat(movement.getProduct()).isEqualTo(testProduct);
        assertThat(movement.getMovementType()).isEqualTo(InventoryMovement.MovementType.IN);
        assertThat(movement.getQuantity()).isEqualTo(10);
        assertThat(movement.getReferenceType()).isEqualTo(InventoryMovement.ReferenceType.PURCHASE_ORDER);
        assertThat(movement.getReferenceId()).isEqualTo(1L);
    }

    @Test
    void createInventoryMovement_WithNullProduct_ShouldFailValidation() {
        // Given
        InventoryMovement movement = new InventoryMovement(null, InventoryMovement.MovementType.IN, 10);

        // When
        Set<ConstraintViolation<InventoryMovement>> violations = validator.validate(movement);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("must not be null");
    }

    @Test
    void createInventoryMovement_WithNullMovementType_ShouldFailValidation() {
        // Given
        InventoryMovement movement = new InventoryMovement(testProduct, null, 10);

        // When
        Set<ConstraintViolation<InventoryMovement>> violations = validator.validate(movement);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("must not be null");
    }

    @Test
    void createInventoryMovement_WithNullQuantity_ShouldFailValidation() {
        // Given
        InventoryMovement movement = new InventoryMovement(testProduct, InventoryMovement.MovementType.IN, null);

        // When
        Set<ConstraintViolation<InventoryMovement>> violations = validator.validate(movement);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("must not be null");
    }

    @Test
    void isInbound_WithInMovementType_ShouldReturnTrue() {
        // Given
        InventoryMovement movement = new InventoryMovement(testProduct, InventoryMovement.MovementType.IN, 10);

        // When & Then
        assertThat(movement.isInbound()).isTrue();
        assertThat(movement.isOutbound()).isFalse();
    }

    @Test
    void isOutbound_WithOutMovementType_ShouldReturnTrue() {
        // Given
        InventoryMovement movement = new InventoryMovement(testProduct, InventoryMovement.MovementType.OUT, 10);

        // When & Then
        assertThat(movement.isOutbound()).isTrue();
        assertThat(movement.isInbound()).isFalse();
    }

    @Test
    void getSignedQuantity_WithInMovementType_ShouldReturnPositive() {
        // Given
        InventoryMovement movement = new InventoryMovement(testProduct, InventoryMovement.MovementType.IN, 10);

        // When & Then
        assertThat(movement.getSignedQuantity()).isEqualTo(10);
    }

    @Test
    void getSignedQuantity_WithOutMovementType_ShouldReturnNegative() {
        // Given
        InventoryMovement movement = new InventoryMovement(testProduct, InventoryMovement.MovementType.OUT, 10);

        // When & Then
        assertThat(movement.getSignedQuantity()).isEqualTo(-10);
    }

    @Test
    void movementType_AllTypes_ShouldBeValid() {
        // Test all movement types
        InventoryMovement inMovement = new InventoryMovement(testProduct, InventoryMovement.MovementType.IN, 10);
        InventoryMovement outMovement = new InventoryMovement(testProduct, InventoryMovement.MovementType.OUT, 10);

        assertThat(inMovement.getMovementType()).isEqualTo(InventoryMovement.MovementType.IN);
        assertThat(outMovement.getMovementType()).isEqualTo(InventoryMovement.MovementType.OUT);
    }

    @Test
    void referenceType_AllTypes_ShouldBeValid() {
        // Test all reference types
        InventoryMovement movement = new InventoryMovement(testProduct, InventoryMovement.MovementType.IN, 10);

        movement.setReferenceType(InventoryMovement.ReferenceType.PURCHASE_ORDER);
        assertThat(movement.getReferenceType()).isEqualTo(InventoryMovement.ReferenceType.PURCHASE_ORDER);

        movement.setReferenceType(InventoryMovement.ReferenceType.SALE);
        assertThat(movement.getReferenceType()).isEqualTo(InventoryMovement.ReferenceType.SALE);

        movement.setReferenceType(InventoryMovement.ReferenceType.ADJUSTMENT);
        assertThat(movement.getReferenceType()).isEqualTo(InventoryMovement.ReferenceType.ADJUSTMENT);

        movement.setReferenceType(InventoryMovement.ReferenceType.TRANSFER);
        assertThat(movement.getReferenceType()).isEqualTo(InventoryMovement.ReferenceType.TRANSFER);

        movement.setReferenceType(InventoryMovement.ReferenceType.RETURN);
        assertThat(movement.getReferenceType()).isEqualTo(InventoryMovement.ReferenceType.RETURN);
    }

    @Test
    void createInventoryMovement_WithTooLongSourceSystem_ShouldFailValidation() {
        // Given
        InventoryMovement movement = new InventoryMovement(testProduct, InventoryMovement.MovementType.IN, 10);
        movement.setSourceSystem("a".repeat(51)); // Max is 50

        // When
        Set<ConstraintViolation<InventoryMovement>> violations = validator.validate(movement);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("size must be between 0 and 50");
    }

    @Test
    void createInventoryMovement_WithTooLongNotes_ShouldFailValidation() {
        // Given
        InventoryMovement movement = new InventoryMovement(testProduct, InventoryMovement.MovementType.IN, 10);
        movement.setNotes("a".repeat(501)); // Max is 500

        // When
        Set<ConstraintViolation<InventoryMovement>> violations = validator.validate(movement);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("size must be between 0 and 500");
    }
}
