package com.scalecart.service;

import com.scalecart.domain.Category;
import com.scalecart.domain.Product;
import com.scalecart.repository.CategoryRepository;
import com.scalecart.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Cacheable(cacheNames = "products", key = "#id")
    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Transactional
    @CacheEvict(cacheNames = "products", key = "#result.id")
    public Product create(String name, String sku, BigDecimal price, Long categoryId) {
        Product p = new Product();
        p.setName(name);
        p.setSku(sku);
        p.setPrice(price);
        if (categoryId != null) {
            categoryRepository.findById(categoryId).ifPresent(p::setCategory);
        }
        return productRepository.save(p);
    }

    @Transactional
    @CacheEvict(cacheNames = "products", key = "#id")
    public Product update(Long id, String name, String sku, BigDecimal price, Long categoryId) {
        Product p = productRepository.findById(id).orElseThrow();
        p.setName(name);
        p.setSku(sku);
        p.setPrice(price);
        if (categoryId != null) {
            categoryRepository.findById(categoryId).ifPresent(p::setCategory);
        } else {
            p.setCategory(null);
        }
        return productRepository.save(p);
    }

    @Transactional
    @CacheEvict(cacheNames = "products", key = "#id")
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }
}
