package com.scalecart.service;

import com.scalecart.domain.Category;
import com.scalecart.domain.Product;
import com.scalecart.repository.CategoryRepository;
import com.scalecart.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private ProductService productService;

    @Test
    void findById_returnsEmptyWhenNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThat(productService.findById(1L)).isEmpty();
    }

    @Test
    void findById_returnsProductWhenFound() {
        Product p = product();
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        assertThat(productService.findById(1L)).contains(p);
    }

    @Test
    void findAll_returnsPage() {
        List<Product> list = List.of(product());
        when(productRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(list));
        Page<Product> result = productService.findAll(Pageable.unpaged());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Widget");
    }

    @Test
    void create_savesAndReturnsProduct() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category()));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        Product created = productService.create("Widget", "W1", BigDecimal.TEN, 1L);
        assertThat(created.getName()).isEqualTo("Widget");
        assertThat(created.getSku()).isEqualTo("W1");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void update_savesAndReturnsProduct() {
        Product p = product();
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(new Category()));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        Product updated = productService.update(1L, "NewName", "SKU2", BigDecimal.ONE, 2L);
        assertThat(updated.getName()).isEqualTo("NewName");
        verify(productRepository).save(p);
    }

    @Test
    void deleteById_deletes() {
        productService.deleteById(1L);
        verify(productRepository).deleteById(1L);
    }

    private static Product product() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Widget");
        p.setSku("W1");
        p.setPrice(BigDecimal.TEN);
        return p;
    }
}
