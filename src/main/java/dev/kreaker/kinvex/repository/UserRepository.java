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

import dev.kreaker.kinvex.entity.User;
import dev.kreaker.kinvex.entity.User.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Basic finder methods
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Active users
    List<User> findByActiveTrue();

    Page<User> findByActiveTrue(Pageable pageable);

    // Find by role
    List<User> findByRole(UserRole role);

    List<User> findByRoleAndActiveTrue(UserRole role);

    // Custom queries for reports
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT u.role, COUNT(u) FROM User u WHERE u.active = true GROUP BY u.role")
    List<Object[]> countActiveUsersByRole();

    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
    long countUsersCreatedSince(@Param("date") LocalDateTime date);
}
