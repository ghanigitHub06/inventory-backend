package com.inventory.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long   id;
    private String status;        // "PENDING", "PROCESSING", etc.
    private BigDecimal totalPrice;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Nested DTO for each line item ────────────────────────────────────────
    // Static inner class so it lives in the same file but is referenced as
    // OrderResponse.OrderItemResponse elsewhere in the codebase
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long       productId;
        private String     productName;
        private Integer    quantity;
        private BigDecimal unitPrice;   // price at the time the order was placed
        private BigDecimal lineTotal;   // unitPrice × quantity
    }
}