package com.scalecart.service;

import com.scalecart.domain.Category;
import com.scalecart.repository.CategoryRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Cacheable(cacheNames = "categories", key = "#id")
    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional
    @CacheEvict(cacheNames = "categories", key = "#result.id")
    public Category create(String name) {
        Category c = new Category();
        c.setName(name);
        return categoryRepository.save(c);
    }

    @Transactional
    @CacheEvict(cacheNames = "categories", key = "#id")
    public Category update(Long id, String name) {
        Category c = categoryRepository.findById(id).orElseThrow();
        c.setName(name);
        return categoryRepository.save(c);
    }
}
