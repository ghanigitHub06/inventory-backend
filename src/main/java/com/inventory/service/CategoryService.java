package com.inventory.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventory.entity.Category;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category create(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("Category already exists: " + name);
        }
        return categoryRepository.save(
            Category.builder().name(name).build()
        );
    }

    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found: " + id);
        }
        // Note: products with this category will have category_id set to NULL
        // because of ON DELETE SET NULL in the schema
        categoryRepository.deleteById(id);
    }
}