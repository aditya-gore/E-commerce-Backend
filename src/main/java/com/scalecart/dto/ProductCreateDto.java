package com.scalecart.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductCreateDto(
    @NotBlank(message = "Name is required")
    @Size(max = 255)
    String name,

    @NotBlank(message = "SKU is required")
    @Size(max = 64)
    String sku,

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    BigDecimal price,

    Long categoryId
) {}
