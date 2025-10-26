package dev.kreaker.kinvex.repository;

import dev.kreaker.kinvex.entity.PurchaseOrder;
import dev.kreaker.kinvex.entity.PurchaseOrder.OrderStatus;
import dev.kreaker.kinvex.entity.Supplier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    // Basic finder methods
    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    // Status-based queries
    List<PurchaseOrder> findByStatus(OrderStatus status);

    Page<PurchaseOrder> findByStatus(OrderStatus status, Pageable pageable);

    List<PurchaseOrder> findByStatusIn(List<OrderStatus> statuses);

    // Supplier-based queries
    List<PurchaseOrder> findBySupplier(Supplier supplier);

    List<PurchaseOrder> findBySupplierId(Long supplierId);

    Page<PurchaseOrder> findBySupplierAndStatus(
            Supplier supplier, OrderStatus status, Pageable pageable);

    // Date-based queries
    List<PurchaseOrder> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);

    List<PurchaseOrder> findByExpectedDateBetween(LocalDate startDate, LocalDate endDate);

    List<PurchaseOrder> findByReceivedDateBetween(LocalDate startDate, LocalDate endDate);

    // Overdue orders
    @Query(
            "SELECT po FROM PurchaseOrder po WHERE po.expectedDate < CURRENT_DATE AND po.status IN ('PENDING', 'CONFIRMED', 'PARTIAL')")
    List<PurchaseOrder> findOverdueOrders();

    @Query(
            "SELECT po FROM PurchaseOrder po WHERE po.expectedDate BETWEEN CURRENT_DATE AND :futureDate AND po.status IN ('PENDING', 'CONFIRMED')")
    List<PurchaseOrder> findOrdersDueSoon(@Param("futureDate") LocalDate futureDate);

    // Custom queries for reports (Requirement 4.4)
    @Query(
            "SELECT po FROM PurchaseOrder po WHERE po.createdAt BETWEEN :startDate AND :endDate ORDER BY po.createdAt DESC")
    List<PurchaseOrder> findOrdersCreatedBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(
            "SELECT po.status, COUNT(po), SUM(po.totalAmount) "
                    + "FROM PurchaseOrder po "
                    + "WHERE po.createdAt BETWEEN :startDate AND :endDate "
                    + "GROUP BY po.status")
    List<Object[]> findOrderStatisticsBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(
            "SELECT MONTH(po.orderDate), YEAR(po.orderDate), COUNT(po), SUM(po.totalAmount) "
                    + "FROM PurchaseOrder po "
                    + "WHERE po.orderDate BETWEEN :startDate AND :endDate "
                    + "GROUP BY YEAR(po.orderDate), MONTH(po.orderDate) "
                    + "ORDER BY YEAR(po.orderDate), MONTH(po.orderDate)")
    List<Object[]> findMonthlyOrderStatistics(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(
            "SELECT po.supplier, COUNT(po), SUM(po.totalAmount), AVG(po.totalAmount) "
                    + "FROM PurchaseOrder po "
                    + "WHERE po.createdAt BETWEEN :startDate AND :endDate "
                    + "GROUP BY po.supplier ORDER BY SUM(po.totalAmount) DESC")
    List<Object[]> findOrdersBySupplierBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(po) FROM PurchaseOrder po WHERE po.status = :status")
    long countByStatus(@Param("status") OrderStatus status);

    @Query(
            "SELECT SUM(po.totalAmount) FROM PurchaseOrder po WHERE po.status = 'COMPLETED' AND po.receivedDate BETWEEN :startDate AND :endDate")
    BigDecimal sumCompletedOrdersAmountBetween(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(
            "SELECT AVG(CAST((po.receivedDate - po.orderDate) AS int)) "
                    + "FROM PurchaseOrder po "
                    + "WHERE po.status = 'COMPLETED' AND po.receivedDate IS NOT NULL "
                    + "AND po.receivedDate BETWEEN :startDate AND :endDate")
    Double findAverageDeliveryTimeBetween(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
