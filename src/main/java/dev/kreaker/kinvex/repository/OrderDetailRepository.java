package dev.kreaker.kinvex.repository;

import dev.kreaker.kinvex.entity.OrderDetail;
import dev.kreaker.kinvex.entity.Product;
import dev.kreaker.kinvex.entity.PurchaseOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    // Basic finder methods
    List<OrderDetail> findByOrder(PurchaseOrder order);

    List<OrderDetail> findByOrderId(Long orderId);

    List<OrderDetail> findByProduct(Product product);

    List<OrderDetail> findByProductId(Long productId);

    // Partially received orders
    @Query("SELECT od FROM OrderDetail od WHERE od.quantityReceived < od.quantityOrdered")
    List<OrderDetail> findPartiallyReceivedDetails();

    @Query("SELECT od FROM OrderDetail od WHERE od.quantityReceived = 0")
    List<OrderDetail> findUnreceivedDetails();

    @Query("SELECT od FROM OrderDetail od WHERE od.quantityReceived >= od.quantityOrdered")
    List<OrderDetail> findFullyReceivedDetails();

    // Custom queries for reports (Requirement 4.4)
    @Query(
            "SELECT od.product, SUM(od.quantityOrdered), SUM(od.quantityReceived), SUM(od.totalPrice) "
                    + "FROM OrderDetail od "
                    + "JOIN od.order po "
                    + "WHERE po.createdAt BETWEEN :startDate AND :endDate "
                    + "GROUP BY od.product ORDER BY SUM(od.quantityOrdered) DESC")
    List<Object[]> findProductOrderStatisticsBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(
            "SELECT od.product, SUM(od.quantityReceived), AVG(od.unitPrice) "
                    + "FROM OrderDetail od "
                    + "JOIN od.order po "
                    + "WHERE po.status = 'COMPLETED' AND po.receivedDate BETWEEN :startDate AND :endDate "
                    + "GROUP BY od.product ORDER BY SUM(od.quantityReceived) DESC")
    List<Object[]> findMostReceivedProductsBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(
            "SELECT od FROM OrderDetail od "
                    + "JOIN od.order po "
                    + "WHERE po.expectedDate < CURRENT_DATE AND od.quantityReceived < od.quantityOrdered "
                    + "AND po.status IN ('PENDING', 'CONFIRMED', 'PARTIAL')")
    List<OrderDetail> findOverduePartialDetails();

    @Query(
            "SELECT SUM(od.quantityOrdered - od.quantityReceived) "
                    + "FROM OrderDetail od "
                    + "WHERE od.product.id = :productId AND od.quantityReceived < od.quantityOrdered")
    Integer findPendingQuantityForProduct(@Param("productId") Long productId);

    @Query(
            "SELECT od.product, SUM(od.quantityOrdered - od.quantityReceived) "
                    + "FROM OrderDetail od "
                    + "WHERE od.quantityReceived < od.quantityOrdered "
                    + "GROUP BY od.product")
    List<Object[]> findPendingQuantitiesByProduct();

    @Query("SELECT COUNT(od) FROM OrderDetail od WHERE od.quantityReceived < od.quantityOrdered")
    long countPartiallyReceivedDetails();

    @Query(
            "SELECT SUM(od.totalPrice) FROM OrderDetail od JOIN od.order po WHERE po.status = 'COMPLETED'")
    BigDecimal sumCompletedOrderDetailsValue();
}
