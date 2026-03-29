package com.inventory.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.inventory.dto.response.AnalyticsResponse;
import com.inventory.entity.Order;
import com.inventory.entity.User;
import com.inventory.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Customer: view their own orders
    Page<Order> findByUser(User user, Pageable pageable);

    // Admin: filter all orders by status
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    // Admin: find one order but also verify it belongs to the right user
    // Used to prevent customer A from viewing customer B's order
    Optional<Order> findByIdAndUser(Long id, User user);

    // SUM of all delivered orders = total revenue
    // Returns null if no delivered orders exist — handle that in service
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal getTotalRevenue();

    // COUNT of all orders
    // (JpaRepository already gives you count(), but explicit query is clearer)
    @Query("SELECT COUNT(o) FROM Order o")
    long getTotalOrders();

    // Aggregate query to get top products by units sold
    // new DTO(...) inside JPQL is called a "constructor expression"
    // Spring maps the results directly to your DTO class
    @Query("""
        SELECT new com.inventory.dto.response.AnalyticsResponse$TopProductDto(
            p.name,
            SUM(oi.quantity),
            SUM(CAST(oi.quantity AS big_decimal) * oi.unitPrice)
        )
        FROM OrderItem oi
        JOIN oi.product p
        GROUP BY p.id, p.name
        ORDER BY SUM(oi.quantity) DESC
        """)
    List<AnalyticsResponse.TopProductDto> findTopProducts(Pageable pageable);
}