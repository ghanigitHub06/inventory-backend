package com.inventory.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventory.dto.request.ProductRequest;
import com.inventory.dto.response.PagedResponse;
import com.inventory.dto.response.ProductResponse;
import com.inventory.entity.Category;
import com.inventory.entity.Product;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.CategoryRepository;
import com.inventory.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository  productRepository;
    private final CategoryRepository categoryRepository;

    // Any stock count below this triggers a "low stock" flag
    private static final int LOW_STOCK_THRESHOLD = 10;

    // readOnly = true → tells Hibernate not to track changes for dirty checking
    // Slight performance gain on read-heavy endpoints
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getProducts(
            Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            Boolean inStock, int page, int size, String sortBy, String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> result = productRepository.findWithFilters(
            categoryId, minPrice, maxPrice, inStock, pageable
        );

        return buildPagedResponse(result);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return toResponse(product);
    }

    public ProductResponse createProduct(ProductRequest req) {
        Category category = categoryRepository.findById(req.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Category not found: " + req.getCategoryId()));

        Product product = Product.builder()
            .name(req.getName())
            .description(req.getDescription())
            .price(req.getPrice())
            .stock(req.getStock())
            .category(category)
            .build();

        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Long id, ProductRequest req) {
        // Find existing product — fail fast if not found
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        Category category = categoryRepository.findById(req.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Category not found: " + req.getCategoryId()));

        // Update all fields — Hibernate detects changes and generates UPDATE SQL
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setStock(req.getStock());
        product.setCategory(category);

        // No need to call save() explicitly here — the entity is "managed"
        // within the transaction, Hibernate auto-flushes changes at commit
        // But calling save() is fine too and makes intent explicit
        return toResponse(productRepository.save(product));
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts() {
        return productRepository.findByStockLessThan(LOW_STOCK_THRESHOLD)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
            .id(p.getId())
            .name(p.getName())
            .description(p.getDescription())
            .price(p.getPrice())
            .stock(p.getStock())
            .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
            .lowStock(p.getStock() < LOW_STOCK_THRESHOLD)
            .createdAt(p.getCreatedAt())
            .build();
    }

    private PagedResponse<ProductResponse> buildPagedResponse(Page<Product> page) {
        return PagedResponse.<ProductResponse>builder()
            .content(page.getContent().stream().map(this::toResponse).toList())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .build();
    }
}