package com.scalecart.service;

import com.scalecart.domain.Category;
import com.scalecart.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private CategoryService categoryService;

    @Test
    void findById_returnsEmptyWhenNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThat(categoryService.findById(1L)).isEmpty();
    }

    @Test
    void findById_returnsCategoryWhenFound() {
        Category c = category(1L, "Electronics");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c));
        assertThat(categoryService.findById(1L)).contains(c);
    }

    @Test
    void findAll_returnsList() {
        List<Category> list = List.of(category(1L, "A"));
        when(categoryRepository.findAll()).thenReturn(list);
        assertThat(categoryService.findAll()).hasSize(1).element(0).extracting(Category::getName).isEqualTo("A");
    }

    @Test
    void create_savesAndReturnsCategory() {
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        Category created = categoryService.create("Gadgets");
        assertThat(created.getName()).isEqualTo("Gadgets");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void update_savesAndReturnsCategory() {
        Category c = category(1L, "Old");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        Category updated = categoryService.update(1L, "New");
        assertThat(updated.getName()).isEqualTo("New");
        verify(categoryRepository).save(c);
    }

    private static Category category(Long id, String name) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        return c;
    }
}
