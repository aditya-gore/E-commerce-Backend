package com.scalecart.dto;

import java.math.BigDecimal;

public record ProductResponseDto(
    Long id,
    String name,
    String sku,
    BigDecimal price,
    Long categoryId
) {}
