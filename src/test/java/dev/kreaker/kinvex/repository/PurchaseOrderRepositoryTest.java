package dev.kreaker.kinvex.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import dev.kreaker.kinvex.entity.PurchaseOrder;
import dev.kreaker.kinvex.entity.Supplier;
import dev.kreaker.kinvex.entity.User;

@DataJpaTest
@ActiveProfiles("test")
class PurchaseOrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Test
    void findByOrderNumber_ShouldReturnOrder_WhenOrderExists() {
        // Given
        Supplier supplier = new Supplier("Test Supplier", "John Doe", "supplier@example.com", "123-456-7890");
        entityManager.persistAndFlush(supplier);

        User user = new User("testuser", "test@example.com", "hashedpassword", User.UserRole.OPERATOR);
        entityManager.persistAndFlush(user);

        PurchaseOrder order = new PurchaseOrder("PO001", supplier, LocalDate.now());
        order.setCreatedBy(user);
        order.setTotalAmount(new BigDecimal("1000.00"));
        entityManager.persistAndFlush(order);

        // When
        Optional<PurchaseOrder> found = purchaseOrderRepository.findByOrderNumber("PO001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getOrderNumber()).isEqualTo("PO001");
        assertThat(found.get().getSupplier().getName()).isEqualTo("Test Supplier");
    }

    @Test
    void findByStatus_ShouldReturnOrdersWithSpecificStatus() {
        // Given
        Supplier supplier = new Supplier("Test Supplier", "John Doe", "supplier@example.com", "123-456-7890");
        entityManager.persistAndFlush(supplier);

        User user = new User("testuser", "test@example.com", "hashedpassword", User.UserRole.OPERATOR);
        entityManager.persistAndFlush(user);

        PurchaseOrder pendingOrder = new PurchaseOrder("PO001", supplier, LocalDate.now());
        pendingOrder.setStatus(PurchaseOrder.OrderStatus.PENDING);
        pendingOrder.setCreatedBy(user);
        entityManager.persistAndFlush(pendingOrder);

        PurchaseOrder completedOrder = new PurchaseOrder("PO002", supplier, LocalDate.now());
        completedOrder.setStatus(PurchaseOrder.OrderStatus.COMPLETED);
        completedOrder.setCreatedBy(user);
        entityManager.persistAndFlush(completedOrder);

        // When
        List<PurchaseOrder> pendingOrders = purchaseOrderRepository.findByStatus(PurchaseOrder.OrderStatus.PENDING);

        // Then
        assertThat(pendingOrders).hasSize(1);
        assertThat(pendingOrders.get(0).getOrderNumber()).isEqualTo("PO001");
        assertThat(pendingOrders.get(0).getStatus()).isEqualTo(PurchaseOrder.OrderStatus.PENDING);
    }

    @Test
    void findOverdueOrders_ShouldReturnOverdueOrders() {
        // Given
        Supplier supplier = new Supplier("Test Supplier", "John Doe", "supplier@example.com", "123-456-7890");
        entityManager.persistAndFlush(supplier);

        User user = new User("testuser", "test@example.com", "hashedpassword", User.UserRole.OPERATOR);
        entityManager.persistAndFlush(user);

        // Create overdue order
        PurchaseOrder overdueOrder = new PurchaseOrder("PO001", supplier, LocalDate.now().minusDays(10));
        overdueOrder.setExpectedDate(LocalDate.now().minusDays(2));
        overdueOrder.setStatus(PurchaseOrder.OrderStatus.PENDING);
        overdueOrder.setCreatedBy(user);
        entityManager.persistAndFlush(overdueOrder);

        // Create non-overdue order
        PurchaseOrder normalOrder = new PurchaseOrder("PO002", supplier, LocalDate.now());
        normalOrder.setExpectedDate(LocalDate.now().plusDays(5));
        normalOrder.setStatus(PurchaseOrder.OrderStatus.PENDING);
        normalOrder.setCreatedBy(user);
        entityManager.persistAndFlush(normalOrder);

        // When
        List<PurchaseOrder> overdueOrders = purchaseOrderRepository.findOverdueOrders();

        // Then
        assertThat(overdueOrders).hasSize(1);
        assertThat(overdueOrders.get(0).getOrderNumber()).isEqualTo("PO001");
    }

    @Test
    void findOrdersDueSoon_ShouldReturnOrdersDueWithinTimeframe() {
        // Given
        Supplier supplier = new Supplier("Test Supplier", "John Doe", "supplier@example.com", "123-456-7890");
        entityManager.persistAndFlush(supplier);

        User user = new User("testuser", "test@example.com", "hashedpassword", User.UserRole.OPERATOR);
        entityManager.persistAndFlush(user);

        // Create order due soon
        PurchaseOrder dueSoonOrder = new PurchaseOrder("PO001", supplier, LocalDate.now());
        dueSoonOrder.setExpectedDate(LocalDate.now().plusDays(2));
        dueSoonOrder.setStatus(PurchaseOrder.OrderStatus.PENDING);
        dueSoonOrder.setCreatedBy(user);
        entityManager.persistAndFlush(dueSoonOrder);

        // Create order due later
        PurchaseOrder dueLaterOrder = new PurchaseOrder("PO002", supplier, LocalDate.now());
        dueLaterOrder.setExpectedDate(LocalDate.now().plusDays(10));
        dueLaterOrder.setStatus(PurchaseOrder.OrderStatus.PENDING);
        dueLaterOrder.setCreatedBy(user);
        entityManager.persistAndFlush(dueLaterOrder);

        // When
        List<PurchaseOrder> ordersDueSoon = purchaseOrderRepository.findOrdersDueSoon(LocalDate.now().plusDays(5));

        // Then
        assertThat(ordersDueSoon).hasSize(1);
        assertThat(ordersDueSoon.get(0).getOrderNumber()).isEqualTo("PO001");
    }

    @Test
    void findOrderStatisticsBetween_ShouldReturnCorrectStatistics() {
        // Given
        Supplier supplier = new Supplier("Test Supplier", "John Doe", "supplier@example.com", "123-456-7890");
        entityManager.persistAndFlush(supplier);

        User user = new User("testuser", "test@example.com", "hashedpassword", User.UserRole.OPERATOR);
        entityManager.persistAndFlush(user);

        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // Create orders with different statuses
        PurchaseOrder pendingOrder = new PurchaseOrder("PO001", supplier, LocalDate.now());
        pendingOrder.setStatus(PurchaseOrder.OrderStatus.PENDING);
        pendingOrder.setTotalAmount(new BigDecimal("1000.00"));
        pendingOrder.setCreatedBy(user);
        entityManager.persistAndFlush(pendingOrder);

        PurchaseOrder completedOrder = new PurchaseOrder("PO002", supplier, LocalDate.now());
        completedOrder.setStatus(PurchaseOrder.OrderStatus.COMPLETED);
        completedOrder.setTotalAmount(new BigDecimal("2000.00"));
        completedOrder.setCreatedBy(user);
        entityManager.persistAndFlush(completedOrder);

        // When
        List<Object[]> statistics = purchaseOrderRepository.findOrderStatisticsBetween(startDate, endDate);

        // Then
        assertThat(statistics).hasSize(2);

        // Find the PENDING status statistics
        Object[] pendingStats = statistics.stream()
                .filter(stat -> stat[0] == PurchaseOrder.OrderStatus.PENDING)
                .findFirst()
                .orElse(null);

        assertThat(pendingStats).isNotNull();
        assertThat(pendingStats[1]).isEqualTo(1L); // count
        assertThat(pendingStats[2]).isEqualTo(new BigDecimal("1000.00")); // sum
    }

    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        // Given
        Supplier supplier = new Supplier("Test Supplier", "John Doe", "supplier@example.com", "123-456-7890");
        entityManager.persistAndFlush(supplier);

        User user = new User("testuser", "test@example.com", "hashedpassword", User.UserRole.OPERATOR);
        entityManager.persistAndFlush(user);

        // Create multiple pending orders
        for (int i = 1; i <= 3; i++) {
            PurchaseOrder order = new PurchaseOrder("PO00" + i, supplier, LocalDate.now());
            order.setStatus(PurchaseOrder.OrderStatus.PENDING);
            order.setCreatedBy(user);
            entityManager.persistAndFlush(order);
        }

        // Create one completed order
        PurchaseOrder completedOrder = new PurchaseOrder("PO004", supplier, LocalDate.now());
        completedOrder.setStatus(PurchaseOrder.OrderStatus.COMPLETED);
        completedOrder.setCreatedBy(user);
        entityManager.persistAndFlush(completedOrder);

        // When
        long pendingCount = purchaseOrderRepository.countByStatus(PurchaseOrder.OrderStatus.PENDING);
        long completedCount = purchaseOrderRepository.countByStatus(PurchaseOrder.OrderStatus.COMPLETED);

        // Then
        assertThat(pendingCount).isEqualTo(3);
        assertThat(completedCount).isEqualTo(1);
    }

    @Test
    void existsByOrderNumber_ShouldReturnTrue_WhenOrderExists() {
        // Given
        Supplier supplier = new Supplier("Test Supplier", "John Doe", "supplier@example.com", "123-456-7890");
        entityManager.persistAndFlush(supplier);

        User user = new User("testuser", "test@example.com", "hashedpassword", User.UserRole.OPERATOR);
        entityManager.persistAndFlush(user);

        PurchaseOrder order = new PurchaseOrder("PO001", supplier, LocalDate.now());
        order.setCreatedBy(user);
        entityManager.persistAndFlush(order);

        // When
        boolean exists = purchaseOrderRepository.existsByOrderNumber("PO001");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByOrderNumber_ShouldReturnFalse_WhenOrderDoesNotExist() {
        // When
        boolean exists = purchaseOrderRepository.existsByOrderNumber("NONEXISTENT");

        // Then
        assertThat(exists).isFalse();
    }
}
