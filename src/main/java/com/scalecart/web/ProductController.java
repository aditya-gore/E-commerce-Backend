package com.scalecart.web;

import com.scalecart.domain.Product;
import com.scalecart.dto.ProductCreateDto;
import com.scalecart.dto.ProductResponseDto;
import com.scalecart.exception.NotFoundException;
import com.scalecart.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/products")
@Tag(name = "Products", description = "Product CRUD API")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List products", description = "Returns paginated products. Public.")
    public Page<ProductResponseDto> list(@PageableDefault(size = 20) Pageable pageable) {
        return productService.findAll(pageable)
            .map(ProductController::toResponse);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResponseDto> getById(@PathVariable Long id) {
        return productService.findById(id)
            .map(ProductController::toResponse)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new NotFoundException("Product", id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create product")
    public ResponseEntity<ProductResponseDto> create(@Valid @RequestBody ProductCreateDto dto) {
        Product created = productService.create(dto.name(), dto.sku(), dto.price(), dto.categoryId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product")
    public ResponseEntity<ProductResponseDto> update(@PathVariable Long id, @Valid @RequestBody ProductCreateDto dto) {
        Product updated = productService.update(id, dto.name(), dto.sku(), dto.price(), dto.categoryId());
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    static ProductResponseDto toResponse(Product p) {
        return new ProductResponseDto(
            p.getId(),
            p.getName(),
            p.getSku(),
            p.getPrice(),
            p.getCategory() != null ? p.getCategory().getId() : null
        );
    }
}
