package com.inventory.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products", indexes = {
    // Speeds up filtering by category — without this, every category filter is a full table scan
    @Index(name = "idx_products_category", columnList = "category_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    // TEXT type in Postgres — no length limit, good for long descriptions
    @Column(columnDefinition = "TEXT")
    private String description;

    // BigDecimal for money — NEVER use float/double (floating point precision errors)
    // precision=10, scale=2 → up to 99,999,999.99
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    // LAZY = category data is NOT fetched unless you call product.getCategory()
    // EAGER would join category on every product query — unnecessary overhead
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")   // FK column name in products table
    private Category category;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Hibernate auto-updates this on every UPDATE
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}