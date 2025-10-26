package dev.kreaker.kinvex.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import dev.kreaker.kinvex.entity.Category;
import dev.kreaker.kinvex.entity.InventoryMovement;
import dev.kreaker.kinvex.entity.Product;
import dev.kreaker.kinvex.entity.User;

@DataJpaTest
@ActiveProfiles("test")
class InventoryMovementRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Test
    void findMovementsBetween_ShouldReturnMovementsInDateRange() {
        // Given
        Category category = new Category("Electronics");
        entityManager.persistAndFlush(category);

        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        product.setCategory(category);
        entityManager.persistAndFlush(product);

        User user = new User("testuser", "test@example.com", "hashedpassword", User.UserRole.OPERATOR);
        entityManager.persistAndFlush(user);

        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        LocalDateTime movementDate = LocalDateTime.now();

        InventoryMovement movement = new InventoryMovement(product, InventoryMovement.MovementType.IN, 10);
        movement.setCreatedBy(user);
        movement.setReferenceType(InventoryMovement.ReferenceType.PURCHASE_ORDER);
        movement.setReferenceId(1L);
        entityManager.persistAndFlush(movement);

        // When
        List<InventoryMovement> movements = inventoryMovementRepository.findMovementsBetween(startDate, endDate);

        // Then
        assertThat(movements).hasSize(1);
        assertThat(movements.get(0).getProduct().getCode()).isEqualTo("PROD001");
        assertThat(movements.get(0).getMovementType()).isEqualTo(InventoryMovement.MovementType.IN);
        assertThat(movements.get(0).getQuantity()).isEqualTo(10);
    }

    @Test
    void findProductMovementSummaryBetween_ShouldReturnCorrectSummary() {
        // Given
        Category category = new Category("Electronics");
        entityManager.persistAndFlush(category);

        Product product = new Product("PROD001", "Test Product", new BigDecimal("99.99"));
        product.setCategory(category);
        entityManager.persistAndFlush(product);

        User user = new User("testuser", "test@example.com", "hashedpassword", User.UserRole.OPERATOR);
        entityManager.persistAndFlush(user);

        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // Create inbound movement
        InventoryMovement inMovement = new InventoryMovement(product, InventoryMovement.MovementType.IN, 20);
        inMovement.setCreatedBy(user);
        entityManager.persistAndFlush(inMovement);

        // Create outbound movement
        InventoryMovement outMovement = new InventoryMovement(product, InventoryMovement.MovementType.OUT, 5);
        outMovement.setCreatedBy(user);
        entityManager.persistAndFlush(outMovement);

        // When
        List<Object[]> summary = inventoryMovementRepository.findProductMovementSummaryBetween(startDate, endDate);

        // Then
        assertThat(summary).hasSize(1);
        Object[] result = summary.get(0);
        Product resultProduct = (Product) result[0];
        Long inbound = (Long) result[1];
        Long outbound = (Long) result[2];
        Long netMovement = (Long) result[3];

        assertThat(resultProduct.getCode()).isEqualTo("PROD001");
        assertThat(inbound).isEqualTo(20L);
        assertThat(outbound).isEqualTo(5L);
        assertThat(netMovement).isEqualTo(15L);
    }
}
