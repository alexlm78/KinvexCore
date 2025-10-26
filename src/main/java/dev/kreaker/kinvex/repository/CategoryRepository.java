package dev.kreaker.kinvex.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.kreaker.kinvex.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Basic finder methods
    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    // Hierarchical queries
    List<Category> findByParentIsNull();

    List<Category> findByParent(Category parent);

    List<Category> findByParentId(Long parentId);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.name")
    List<Category> findRootCategories();

    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId ORDER BY c.name")
    List<Category> findChildrenByParentId(@Param("parentId") Long parentId);

    // Custom queries for reports
    @Query("SELECT c, COUNT(p) FROM Category c LEFT JOIN c.products p GROUP BY c ORDER BY COUNT(p) DESC")
    List<Object[]> findCategoriesWithProductCount();

    @Query("SELECT c FROM Category c WHERE SIZE(c.products) > 0 ORDER BY c.name")
    List<Category> findCategoriesWithProducts();

    @Query("SELECT c FROM Category c WHERE SIZE(c.products) = 0 ORDER BY c.name")
    List<Category> findEmptyCategories();

    @Query("SELECT c, COUNT(p) FROM Category c LEFT JOIN c.products p WHERE p.active = true GROUP BY c")
    List<Object[]> findCategoriesWithActiveProductCount();
}
