package com.inventory.service;

import java.math.BigDecimal;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventory.dto.response.AnalyticsResponse;
import com.inventory.repository.OrderRepository;
import com.inventory.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository   orderRepository;
    private final ProductRepository productRepository;

    private static final int LOW_STOCK_THRESHOLD = 10;

    @Transactional(readOnly = true)
    public AnalyticsResponse getDashboardStats() {
        BigDecimal revenue = orderRepository.getTotalRevenue();

        return AnalyticsResponse.builder()
            // Null-safe: no delivered orders yet returns null from SUM
            .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
            .totalOrders(orderRepository.getTotalOrders())
            .totalProducts(productRepository.count())
            .lowStockCount(productRepository.countByStockLessThan(LOW_STOCK_THRESHOLD))
            // Top 10 products by units sold
            .topProducts(orderRepository.findTopProducts(PageRequest.of(0, 10)))
            .build();
    }
}
