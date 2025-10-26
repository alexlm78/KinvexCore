package dev.kreaker.kinvex.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.kreaker.kinvex.entity.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // Basic finder methods
    Optional<Supplier> findByName(String name);

    boolean existsByName(String name);

    List<Supplier> findByActiveTrue();

    Page<Supplier> findByActiveTrue(Pageable pageable);

    // Search methods
    List<Supplier> findByNameContainingIgnoreCase(String name);

    List<Supplier> findByContactPersonContainingIgnoreCase(String contactPerson);

    List<Supplier> findByEmailContainingIgnoreCase(String email);

    // Custom queries for reports (Requirement 4.4)
    @Query("SELECT s, COUNT(po), AVG(po.totalAmount), "
            + "COUNT(CASE WHEN po.status = 'COMPLETED' THEN 1 END) as completedOrders, "
            + "COUNT(CASE WHEN po.status = 'CANCELLED' THEN 1 END) as cancelledOrders "
            + "FROM Supplier s LEFT JOIN s.purchaseOrders po "
            + "WHERE po.createdAt BETWEEN :startDate AND :endDate "
            + "GROUP BY s ORDER BY COUNT(po) DESC")
    List<Object[]> findSupplierPerformanceBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Supplier s "
            + "JOIN s.purchaseOrders po "
            + "WHERE po.expectedDate < CURRENT_DATE AND po.status IN ('PENDING', 'CONFIRMED') "
            + "GROUP BY s")
    List<Supplier> findSuppliersWithOverdueOrders();

    @Query("SELECT s, AVG(CASE WHEN po.receivedDate IS NOT NULL AND po.expectedDate IS NOT NULL "
            + "THEN CAST((po.receivedDate - po.expectedDate) AS int) ELSE NULL END) as avgDeliveryDelay "
            + "FROM Supplier s LEFT JOIN s.purchaseOrders po "
            + "WHERE po.status = 'COMPLETED' AND po.createdAt BETWEEN :startDate AND :endDate "
            + "GROUP BY s HAVING AVG(CASE WHEN po.receivedDate IS NOT NULL AND po.expectedDate IS NOT NULL "
            + "THEN CAST((po.receivedDate - po.expectedDate) AS int) ELSE NULL END) IS NOT NULL "
            + "ORDER BY avgDeliveryDelay")
    List<Object[]> findSupplierDeliveryPerformanceBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s, COUNT(po), SUM(po.totalAmount) "
            + "FROM Supplier s LEFT JOIN s.purchaseOrders po "
            + "WHERE po.status = 'COMPLETED' AND po.createdAt BETWEEN :startDate AND :endDate "
            + "GROUP BY s ORDER BY SUM(po.totalAmount) DESC")
    List<Object[]> findTopSuppliersByVolumeBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(s) FROM Supplier s WHERE s.active = true")
    long countActiveSuppliers();

    @Query("SELECT s FROM Supplier s WHERE SIZE(s.purchaseOrders) = 0 AND s.active = true")
    List<Supplier> findSuppliersWithoutOrders();
}
