package com.inventory.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AnalyticsResponse {
    private BigDecimal totalRevenue;
    private long totalOrders;
    private long totalProducts;
    private long lowStockCount;
    private List<TopProductDto> topProducts;

    @Data @Builder
    public static class TopProductDto {
        private String productName;
        private long unitsSold;
        private BigDecimal revenue;
    }
}
