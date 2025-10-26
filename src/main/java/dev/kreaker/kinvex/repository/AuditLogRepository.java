package dev.kreaker.kinvex.repository;

import dev.kreaker.kinvex.entity.AuditLog;
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
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Basic finder methods
    List<AuditLog> findByUser(User user);

    List<AuditLog> findByUserId(Long userId);

    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    List<AuditLog> findByAction(String action);

    List<AuditLog> findByEntityType(String entityType);

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    // Date-based queries
    List<AuditLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    Page<AuditLog> findByCreatedAtBetween(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Combined queries
    List<AuditLog> findByUserAndActionAndCreatedAtBetween(
            User user, String action, LocalDateTime startDate, LocalDateTime endDate);

    List<AuditLog> findByEntityTypeAndCreatedAtBetween(
            String entityType, LocalDateTime startDate, LocalDateTime endDate);

    // Custom queries for audit reports
    @Query(
            "SELECT al FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    List<AuditLog> findAuditLogsBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(
            "SELECT al.action, COUNT(al) FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate GROUP BY al.action ORDER BY COUNT(al) DESC")
    List<Object[]> findActionStatisticsBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(
            "SELECT al.entityType, COUNT(al) FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate GROUP BY al.entityType ORDER BY COUNT(al) DESC")
    List<Object[]> findEntityTypeStatisticsBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(
            "SELECT al.user, COUNT(al) FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate GROUP BY al.user ORDER BY COUNT(al) DESC")
    List<Object[]> findUserActivityStatisticsBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(
            "SELECT DATE(al.createdAt), COUNT(al) FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate GROUP BY DATE(al.createdAt) ORDER BY DATE(al.createdAt)")
    List<Object[]> findDailyActivityBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(
            "SELECT al FROM AuditLog al WHERE al.entityType = :entityType AND al.entityId = :entityId ORDER BY al.createdAt DESC")
    List<AuditLog> findEntityHistory(
            @Param("entityType") String entityType, @Param("entityId") Long entityId);

    @Query(
            "SELECT al FROM AuditLog al WHERE al.user.id = :userId AND al.action IN :actions AND al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    List<AuditLog> findUserActionsBetween(
            @Param("userId") Long userId,
            @Param("actions") List<String> actions,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Security-related queries
    @Query(
            "SELECT al FROM AuditLog al WHERE al.action IN ('LOGIN', 'LOGOUT') AND al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    List<AuditLog> findLoginActivityBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(
            "SELECT al.ipAddress, COUNT(al) FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate GROUP BY al.ipAddress ORDER BY COUNT(al) DESC")
    List<Object[]> findActivityByIpAddressBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Aggregation queries
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.action = :action")
    long countByAction(@Param("action") String action);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.entityType = :entityType")
    long countByEntityType(@Param("entityType") String entityType);

    @Query(
            "SELECT COUNT(DISTINCT al.user) FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate")
    long countDistinctActiveUsersBetween(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
