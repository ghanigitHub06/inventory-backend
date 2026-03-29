package com.inventory.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventory.dto.request.OrderRequest;
import com.inventory.dto.response.OrderResponse;
import com.inventory.dto.response.PagedResponse;
import com.inventory.entity.Order;
import com.inventory.entity.OrderItem;
import com.inventory.entity.Product;
import com.inventory.entity.User;
import com.inventory.enums.OrderStatus;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.OrderRepository;
import com.inventory.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository   orderRepository;
    private final ProductRepository productRepository;

    // Defines every valid status transition
    // Anything not listed here is illegal
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
        OrderStatus.PENDING,    Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
        OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPED,    OrderStatus.CANCELLED),
        OrderStatus.SHIPPED,    Set.of(OrderStatus.DELIVERED),
        OrderStatus.DELIVERED,  Set.of(),   // terminal state
        OrderStatus.CANCELLED,  Set.of()    // terminal state
    );

    public OrderResponse placeOrder(OrderRequest req, User currentUser) {
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (OrderRequest.OrderItemRequest itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Product not found: " + itemReq.getProductId()));

            // Check stock BEFORE decrementing — fail the whole order if anything is short
            if (product.getStock() < itemReq.getQuantity()) {
                throw new InsufficientStockException(
                    "Only " + product.getStock() + " units left for: " + product.getName());
            }

            // Deduct stock — Hibernate will UPDATE this in the same transaction
            product.setStock(product.getStock() - itemReq.getQuantity());

            // Snapshot the current price — historical accuracy
            BigDecimal lineTotal = product.getPrice()
                .multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(lineTotal);

            items.add(OrderItem.builder()
                .product(product)
                .quantity(itemReq.getQuantity())
                .unitPrice(product.getPrice())   // snapshot price at time of order
                .build());
        }

        Order order = Order.builder()
            .user(currentUser)
            .status(OrderStatus.PENDING)
            .totalPrice(total)
            .items(items)
            .build();

        // Link each item back to the order (required for the FK)
        items.forEach(item -> item.setOrder(order));

        // save() persists order + all items via CascadeType.ALL
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        // Validate the transition is legal
        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.get(order.getStatus());
        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException(
                "Cannot transition from " + order.getStatus() + " to " + newStatus
                + ". Allowed: " + allowed);
        }

        order.setStatus(newStatus);
        return toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getUserOrders(User user, int page, int size) {
        Page<Order> result = orderRepository.findByUser(
            user, PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return buildPagedResponse(result);
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getAllOrders(OrderStatus status, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> result = (status != null)
            ? orderRepository.findByStatus(status, pageable)
            : orderRepository.findAll(pageable);
        return buildPagedResponse(result);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderForUser(Long orderId, User user) {
        Order order = orderRepository.findByIdAndUser(orderId, user)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return toResponse(order);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private OrderResponse toResponse(Order o) {
        List<OrderResponse.OrderItemResponse> itemResponses = o.getItems().stream()
            .map(item -> OrderResponse.OrderItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity())))
                .build())
            .toList();

        return OrderResponse.builder()
            .id(o.getId())
            .status(o.getStatus().name())
            .totalPrice(o.getTotalPrice())
            .items(itemResponses)
            .createdAt(o.getCreatedAt())
            .updatedAt(o.getUpdatedAt())
            .build();
    }

    private PagedResponse<OrderResponse> buildPagedResponse(Page<Order> page) {
        return PagedResponse.<OrderResponse>builder()
            .content(page.getContent().stream().map(this::toResponse).toList())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .build();
    }
}
