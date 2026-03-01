package com.scalecart.web;

import com.scalecart.domain.Category;
import com.scalecart.dto.CategoryCreateDto;
import com.scalecart.dto.CategoryResponseDto;
import com.scalecart.exception.NotFoundException;
import com.scalecart.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/categories")
@Tag(name = "Categories", description = "Category API")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List all categories. Public.")
    public List<CategoryResponseDto> list() {
        return categoryService.findAll().stream()
            .map(c -> new CategoryResponseDto(c.getId(), c.getName()))
            .toList();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryResponseDto> getById(@PathVariable Long id) {
        return categoryService.findById(id)
            .map(c -> ResponseEntity.ok(new CategoryResponseDto(c.getId(), c.getName())))
            .orElseThrow(() -> new NotFoundException("Category", id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create category")
    public ResponseEntity<CategoryResponseDto> create(@Valid @RequestBody CategoryCreateDto dto) {
        Category created = categoryService.create(dto.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(new CategoryResponseDto(created.getId(), created.getName()));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update category")
    public ResponseEntity<CategoryResponseDto> update(@PathVariable Long id, @Valid @RequestBody CategoryCreateDto dto) {
        Category updated = categoryService.update(id, dto.name());
        return ResponseEntity.ok(new CategoryResponseDto(updated.getId(), updated.getName()));
    }
}
