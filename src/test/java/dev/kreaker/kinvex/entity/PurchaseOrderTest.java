package dev.kreaker.kinvex.entity;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PurchaseOrderTest {

    private Validator validator;
    private Supplier testSupplier;
    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        testSupplier =
                new Supplier("Test Supplier", "John Doe", "supplier@example.com", "123-456-7890");
        testUser =
                new User("testuser", "test@example.com", "hashedpassword", User.UserRole.OPERATOR);
        testProduct = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
    }

    @Test
    void createPurchaseOrder_WithValidData_ShouldPass() {
        // Given
        LocalDate orderDate = LocalDate.now();
        LocalDate expectedDate = LocalDate.now().plusDays(7);
        PurchaseOrder order =
                new PurchaseOrder("PO001", testSupplier, orderDate, expectedDate, testUser);

        // When
        Set<ConstraintViolation<PurchaseOrder>> violations = validator.validate(order);

        // Then
        assertThat(violations).isEmpty();
        assertThat(order.getOrderNumber()).isEqualTo("PO001");
        assertThat(order.getSupplier()).isEqualTo(testSupplier);
        assertThat(order.getOrderDate()).isEqualTo(orderDate);
        assertThat(order.getExpectedDate()).isEqualTo(expectedDate);
        assertThat(order.getStatus()).isEqualTo(PurchaseOrder.OrderStatus.PENDING);
        assertThat(order.getCreatedBy()).isEqualTo(testUser);
    }

    @Test
    void createPurchaseOrder_WithBlankOrderNumber_ShouldFailValidation() {
        // Given
        PurchaseOrder order = new PurchaseOrder("", testSupplier, LocalDate.now());

        // When
        Set<ConstraintViolation<PurchaseOrder>> violations = validator.validate(order);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("must not be blank");
    }

    @Test
    void createPurchaseOrder_WithNullSupplier_ShouldFailValidation() {
        // Given
        PurchaseOrder order = new PurchaseOrder("PO001", null, LocalDate.now());

        // When
        Set<ConstraintViolation<PurchaseOrder>> violations = validator.validate(order);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("must not be null");
    }

    @Test
    void createPurchaseOrder_WithNullOrderDate_ShouldFailValidation() {
        // Given
        PurchaseOrder order = new PurchaseOrder("PO001", testSupplier, null);

        // When
        Set<ConstraintViolation<PurchaseOrder>> violations = validator.validate(order);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("must not be null");
    }

    @Test
    void addOrderDetail_ShouldAddDetailAndCalculateTotal() {
        // Given
        PurchaseOrder order = new PurchaseOrder("PO001", testSupplier, LocalDate.now());
        OrderDetail detail = new OrderDetail(order, testProduct, 10, new BigDecimal("99.99"));

        // When
        order.addOrderDetail(detail);

        // Then
        assertThat(order.getOrderDetails()).hasSize(1);
        assertThat(order.getOrderDetails().get(0)).isEqualTo(detail);
        assertThat(detail.getOrder()).isEqualTo(order);
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("999.90"));
    }

    @Test
    void removeOrderDetail_ShouldRemoveDetailAndRecalculateTotal() {
        // Given
        PurchaseOrder order = new PurchaseOrder("PO001", testSupplier, LocalDate.now());
        OrderDetail detail1 = new OrderDetail(order, testProduct, 10, new BigDecimal("99.99"));
        OrderDetail detail2 = new OrderDetail(order, testProduct, 5, new BigDecimal("50.00"));

        order.addOrderDetail(detail1);
        order.addOrderDetail(detail2);

        // When
        order.removeOrderDetail(detail1);

        // Then
        assertThat(order.getOrderDetails()).hasSize(1);
        assertThat(order.getOrderDetails().get(0)).isEqualTo(detail2);
        assertThat(detail1.getOrder()).isNull();
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("250.00"));
    }

    @Test
    void calculateTotalAmount_WithMultipleDetails_ShouldCalculateCorrectTotal() {
        // Given
        PurchaseOrder order = new PurchaseOrder("PO001", testSupplier, LocalDate.now());
        OrderDetail detail1 = new OrderDetail(order, testProduct, 10, new BigDecimal("99.99"));
        OrderDetail detail2 = new OrderDetail(order, testProduct, 5, new BigDecimal("50.00"));

        order.addOrderDetail(detail1);
        order.addOrderDetail(detail2);

        // When
        order.calculateTotalAmount();

        // Then
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("1249.90"));
    }

    @Test
    void isOverdue_WhenExpectedDatePassed_ShouldReturnTrue() {
        // Given
        LocalDate pastDate = LocalDate.now().minusDays(5);
        PurchaseOrder order = new PurchaseOrder("PO001", testSupplier, LocalDate.now());
        order.setExpectedDate(pastDate);
        order.setStatus(PurchaseOrder.OrderStatus.PENDING);

        // When & Then
        assertThat(order.isOverdue()).isTrue();
    }

    @Test
    void isOverdue_WhenExpectedDateNotPassed_ShouldReturnFalse() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(5);
        PurchaseOrder order = new PurchaseOrder("PO001", testSupplier, LocalDate.now());
        order.setExpectedDate(futureDate);
        order.setStatus(PurchaseOrder.OrderStatus.PENDING);

        // When & Then
        assertThat(order.isOverdue()).isFalse();
    }

    @Test
    void isOverdue_WhenOrderCompleted_ShouldReturnFalse() {
        // Given
        LocalDate pastDate = LocalDate.now().minusDays(5);
        PurchaseOrder order = new PurchaseOrder("PO001", testSupplier, LocalDate.now());
        order.setExpectedDate(pastDate);
        order.setStatus(PurchaseOrder.OrderStatus.COMPLETED);

        // When & Then
        assertThat(order.isOverdue()).isFalse();
    }

    @Test
    void isFullyReceived_WhenAllQuantitiesReceived_ShouldReturnTrue() {
        // Given
        PurchaseOrder order = new PurchaseOrder("PO001", testSupplier, LocalDate.now());
        OrderDetail detail1 = new OrderDetail(order, testProduct, 10, new BigDecimal("99.99"));
        detail1.setQuantityReceived(10);
        OrderDetail detail2 = new OrderDetail(order, testProduct, 5, new BigDecimal("50.00"));
        detail2.setQuantityReceived(5);

        order.addOrderDetail(detail1);
        order.addOrderDetail(detail2);

        // When & Then
        assertThat(order.isFullyReceived()).isTrue();
    }

    @Test
    void isFullyReceived_WhenNotAllQuantitiesReceived_ShouldReturnFalse() {
        // Given
        PurchaseOrder order = new PurchaseOrder("PO001", testSupplier, LocalDate.now());
        OrderDetail detail1 = new OrderDetail(order, testProduct, 10, new BigDecimal("99.99"));
        detail1.setQuantityReceived(8); // Not fully received
        OrderDetail detail2 = new OrderDetail(order, testProduct, 5, new BigDecimal("50.00"));
        detail2.setQuantityReceived(5);

        order.addOrderDetail(detail1);
        order.addOrderDetail(detail2);

        // When & Then
        assertThat(order.isFullyReceived()).isFalse();
    }

    @Test
    void isPartiallyReceived_WhenSomeQuantitiesReceived_ShouldReturnTrue() {
        // Given
        PurchaseOrder order = new PurchaseOrder("PO001", testSupplier, LocalDate.now());
        OrderDetail detail1 = new OrderDetail(order, testProduct, 10, new BigDecimal("99.99"));
        detail1.setQuantityReceived(5); // Partially received
        OrderDetail detail2 = new OrderDetail(order, testProduct, 5, new BigDecimal("50.00"));
        detail2.setQuantityReceived(0); // Not received

        order.addOrderDetail(detail1);
        order.addOrderDetail(detail2);

        // When & Then
        assertThat(order.isPartiallyReceived()).isTrue();
        assertThat(order.isFullyReceived()).isFalse();
    }

    @Test
    void orderStatus_AllStatuses_ShouldBeValid() {
        // Test all order statuses
        PurchaseOrder order = new PurchaseOrder("PO001", testSupplier, LocalDate.now());

        order.setStatus(PurchaseOrder.OrderStatus.PENDING);
        assertThat(order.getStatus()).isEqualTo(PurchaseOrder.OrderStatus.PENDING);

        order.setStatus(PurchaseOrder.OrderStatus.CONFIRMED);
        assertThat(order.getStatus()).isEqualTo(PurchaseOrder.OrderStatus.CONFIRMED);

        order.setStatus(PurchaseOrder.OrderStatus.PARTIAL);
        assertThat(order.getStatus()).isEqualTo(PurchaseOrder.OrderStatus.PARTIAL);

        order.setStatus(PurchaseOrder.OrderStatus.COMPLETED);
        assertThat(order.getStatus()).isEqualTo(PurchaseOrder.OrderStatus.COMPLETED);

        order.setStatus(PurchaseOrder.OrderStatus.CANCELLED);
        assertThat(order.getStatus()).isEqualTo(PurchaseOrder.OrderStatus.CANCELLED);
    }
}
