package com.inventory.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.dto.response.PagedResponse;
import com.inventory.dto.response.ProductResponse;
import com.inventory.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // All params are optional — null means "no filter for this field"
    // defaultValue prevents Spring from throwing on missing params
    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> list(
            @RequestParam(required = false)               Long       categoryId,
            @RequestParam(required = false)               BigDecimal minPrice,
            @RequestParam(required = false)               BigDecimal maxPrice,
            @RequestParam(required = false)               Boolean    inStock,
            @RequestParam(defaultValue = "0")             int        page,
            @RequestParam(defaultValue = "20")            int        size,
            @RequestParam(defaultValue = "name")          String     sortBy,
            @RequestParam(defaultValue = "asc")           String     direction) {

        return ResponseEntity.ok(productService.getProducts(
            categoryId, minPrice, maxPrice, inStock, page, size, sortBy, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }
}