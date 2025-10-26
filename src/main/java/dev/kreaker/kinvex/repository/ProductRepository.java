package dev.kreaker.kinvex.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.kreaker.kinvex.entity.Category;
import dev.kreaker.kinvex.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Basic finder methods
    Optional<Product> findByCode(String code);

    boolean existsByCode(String code);

    List<Product> findByActiveTrue();

    Page<Product> findByActiveTrue(Pageable pageable);

    // Search methods
    List<Product> findByNameContainingIgnoreCase(String name);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Product> findByCodeContainingIgnoreCase(String code);

    // Category-based queries
    List<Product> findByCategory(Category category);

    List<Product> findByCategoryId(Long categoryId);

    Page<Product> findByCategoryAndActiveTrue(Category category, Pageable pageable);

    // Stock-related queries
    @Query("SELECT p FROM Product p WHERE p.currentStock <= p.minStock AND p.active = true")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.currentStock > p.maxStock AND p.maxStock IS NOT NULL AND p.active = true")
    List<Product> findOverStockProducts();

    @Query("SELECT p FROM Product p WHERE p.currentStock = 0 AND p.active = true")
    List<Product> findOutOfStockProducts();

    @Query("SELECT p FROM Product p WHERE p.currentStock BETWEEN :minStock AND :maxStock AND p.active = true")
    List<Product> findProductsByStockRange(@Param("minStock") Integer minStock, @Param("maxStock") Integer maxStock);

    // Price-related queries
    @Query("SELECT p FROM Product p WHERE p.unitPrice BETWEEN :minPrice AND :maxPrice AND p.active = true")
    List<Product> findProductsByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    // Custom queries for reports (Requirement 1.3, 4.4)
    @Query("SELECT p, SUM(im.quantity) as totalMovements FROM Product p "
            + "LEFT JOIN p.inventoryMovements im "
            + "WHERE im.createdAt BETWEEN :startDate AND :endDate "
            + "GROUP BY p ORDER BY totalMovements DESC")
    List<Object[]> findProductsWithMovementsBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Product p "
            + "JOIN p.inventoryMovements im "
            + "WHERE im.movementType = 'OUT' AND im.createdAt BETWEEN :startDate AND :endDate "
            + "GROUP BY p ORDER BY SUM(im.quantity) DESC")
    List<Product> findMostSoldProductsBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p, SUM(CASE WHEN im.movementType = 'IN' THEN im.quantity ELSE 0 END) as inbound, "
            + "SUM(CASE WHEN im.movementType = 'OUT' THEN im.quantity ELSE 0 END) as outbound "
            + "FROM Product p LEFT JOIN p.inventoryMovements im "
            + "WHERE im.createdAt BETWEEN :startDate AND :endDate "
            + "GROUP BY p")
    List<Object[]> findProductMovementSummaryBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true")
    long countActiveProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.currentStock <= p.minStock AND p.active = true")
    long countLowStockProducts();

    @Query("SELECT SUM(p.currentStock * p.unitPrice) FROM Product p WHERE p.active = true")
    BigDecimal calculateTotalInventoryValue();

    @Query("SELECT c.name, COUNT(p), AVG(p.currentStock), SUM(p.currentStock * p.unitPrice) "
            + "FROM Product p JOIN p.category c WHERE p.active = true "
            + "GROUP BY c.id, c.name ORDER BY c.name")
    List<Object[]> findInventoryStatsByCategory();
}
