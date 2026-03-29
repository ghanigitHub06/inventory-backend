package com.inventory.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.inventory.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // JPQL query — works on entity field names (price, category.id, stock)
    // NOT SQL column names (product_price, category_id)
    //
    // The trick: (:param IS NULL OR condition) means "if no filter passed, skip it"
    // This one query handles all filter combinations — no need for 8 separate methods
    @Query("""
        SELECT p FROM Product p
        LEFT JOIN p.category c
        WHERE (:categoryId IS NULL OR c.id = :categoryId)
          AND (:minPrice   IS NULL OR p.price >= :minPrice)
          AND (:maxPrice   IS NULL OR p.price <= :maxPrice)
          AND (:inStock    IS NULL
               OR (:inStock = true  AND p.stock > 0)
               OR (:inStock = false AND p.stock = 0))
        """)
    Page<Product> findWithFilters(
        @Param("categoryId") Long categoryId,
        @Param("minPrice")   BigDecimal minPrice,
        @Param("maxPrice")   BigDecimal maxPrice,
        @Param("inStock")    Boolean inStock,
        Pageable pageable    // Spring injects ORDER BY, LIMIT, OFFSET from this
    );

    // Returns all products below a stock threshold — used for low-stock alerts
    List<Product> findByStockLessThan(int threshold);

    // COUNT query — used in analytics, much faster than loading all products
    long countByStockLessThan(int threshold);

    // Used by admin search
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}