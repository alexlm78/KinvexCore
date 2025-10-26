package dev.kreaker.kinvex.repository;

import dev.kreaker.kinvex.entity.InventoryMovement;
import dev.kreaker.kinvex.entity.InventoryMovement.MovementType;
import dev.kreaker.kinvex.entity.InventoryMovement.ReferenceType;
import dev.kreaker.kinvex.entity.Product;
import dev.kreaker.kinvex.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    // Basic finder methods
    List<InventoryMovement> findByProduct(Product product);

    List<InventoryMovement> findByProductId(Long productId);

    Page<InventoryMovement> findByProductId(Long productId, Pageable pageable);

    List<InventoryMovement> findByMovementType(MovementType movementType);

    List<InventoryMovement> findByReferenceType(ReferenceType referenceType);

    List<InventoryMovement> findByReferenceTypeAndReferenceId(
            ReferenceType referenceType, Long referenceId);

    List<InventoryMovement> findByCreatedBy(User createdBy);

    List<InventoryMovement> findBySourceSystem(String sourceSystem);

    // Date-based queries
    List<InventoryMovement> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    Page<InventoryMovement> findByCreatedAtBetween(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    List<InventoryMovement> findByProductAndCreatedAtBetween(
            Product product, LocalDateTime startDate, LocalDateTime endDate);

    // Custom queries for reports (Requirements 1.3, 4.4)
    @Query(
            "SELECT im FROM InventoryMovement im WHERE im.createdAt BETWEEN :startDate AND :endDate ORDER BY im.createdAt DESC")
    List<InventoryMovement> findMovementsBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(
            "SELECT im.product, SUM(CASE WHEN im.movementType = 'IN' THEN im.quantity ELSE 0 END) as inbound, "
                    + "SUM(CASE WHEN im.movementType = 'OUT' THEN im.quantity ELSE 0 END) as outbound, "
                    + "SUM(CASE WHEN im.movementType = 'IN' THEN im.quantity ELSE -im.quantity END) as netMovement "
                    + "FROM InventoryMovement im "
                    + "WHERE im.createdAt BETWEEN :startDate AND :endDate "
                    + "GROUP BY im.product ORDER BY netMovement DESC")
    List<Object[]> findProductMovementSummaryBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(
            "SELECT im.movementType, im.referenceType, COUNT(im), SUM(im.quantity) "
                    + "FROM InventoryMovement im "
                    + "WHERE im.createdAt BETWEEN :startDate AND :endDate "
                    + "GROUP BY im.movementType, im.referenceType")
    List<Object[]> findMovementStatisticsByTypeBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(
            "SELECT DATE(im.createdAt), SUM(CASE WHEN im.movementType = 'IN' THEN im.quantity ELSE 0 END), "
                    + "SUM(CASE WHEN im.movementType = 'OUT' THEN im.quantity ELSE 0 END) "
                    + "FROM InventoryMovement im "
                    + "WHERE im.createdAt BETWEEN :startDate AND :endDate "
                    + "GROUP BY DATE(im.createdAt) ORDER BY DATE(im.createdAt)")
    List<Object[]> findDailyMovementSummaryBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(
            "SELECT im.sourceSystem, COUNT(im), SUM(im.quantity) "
                    + "FROM InventoryMovement im "
                    + "WHERE im.movementType = 'OUT' AND im.createdAt BETWEEN :startDate AND :endDate "
                    + "GROUP BY im.sourceSystem ORDER BY SUM(im.quantity) DESC")
    List<Object[]> findOutboundMovementsBySourceBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(
            "SELECT im.product, SUM(im.quantity) "
                    + "FROM InventoryMovement im "
                    + "WHERE im.movementType = 'OUT' AND im.createdAt BETWEEN :startDate AND :endDate "
                    + "GROUP BY im.product ORDER BY SUM(im.quantity) DESC")
    List<Object[]> findMostSoldProductsBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(
            "SELECT im.product, SUM(im.quantity) "
                    + "FROM InventoryMovement im "
                    + "WHERE im.movementType = 'IN' AND im.createdAt BETWEEN :startDate AND :endDate "
                    + "GROUP BY im.product ORDER BY SUM(im.quantity) DESC")
    List<Object[]> findMostReceivedProductsBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Aggregation queries
    @Query("SELECT COUNT(im) FROM InventoryMovement im WHERE im.movementType = :movementType")
    long countByMovementType(@Param("movementType") MovementType movementType);

    @Query(
            "SELECT SUM(im.quantity) FROM InventoryMovement im WHERE im.movementType = :movementType AND im.createdAt BETWEEN :startDate AND :endDate")
    Long sumQuantityByMovementTypeBetween(
            @Param("movementType") MovementType movementType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(
            "SELECT COUNT(DISTINCT im.product) FROM InventoryMovement im WHERE im.createdAt BETWEEN :startDate AND :endDate")
    long countDistinctProductsWithMovementsBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
