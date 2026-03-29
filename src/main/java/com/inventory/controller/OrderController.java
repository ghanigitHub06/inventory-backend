package com.inventory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.dto.request.OrderRequest;
import com.inventory.dto.response.OrderResponse;
import com.inventory.dto.response.PagedResponse;
import com.inventory.entity.User;
import com.inventory.enums.OrderStatus;
import com.inventory.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // @AuthenticationPrincipal injects the currently logged-in User object
    // Spring Security extracts this from the JWT via UserDetailsServiceImpl
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody OrderRequest req,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(201).body(orderService.placeOrder(req, currentUser));
    }

    // Customer: view own orders
    @GetMapping("/orders/my")
    public ResponseEntity<PagedResponse<OrderResponse>> myOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getUserOrders(user, page, size));
    }

    // Customer: view one specific order (service verifies ownership)
    @GetMapping("/orders/my/{id}")
    public ResponseEntity<OrderResponse> myOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.getOrderForUser(id, user));
    }

    // Admin: view all orders, optionally filtered by status
    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<OrderResponse>> allOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderService.getAllOrders(status, page, size));
    }

    // Admin: move order through status flow
    @PatchMapping("/admin/orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }
}
