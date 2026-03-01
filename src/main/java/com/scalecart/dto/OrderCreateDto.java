package com.scalecart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderCreateDto(
    @NotNull
    Long customerId,

    @NotNull
    @Size(min = 1)
    List<@Valid OrderItemDto> items
) {
    public record OrderItemDto(
        @NotNull Long productId,
        @NotNull Integer quantity
    ) {}
}
