package com.scalecart.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponseDto(
    Long id,
    Long customerId,
    String status,
    BigDecimal totalAmount,
    Instant createdAt,
    List<OrderItemResponseDto> items
) {
    public record OrderItemResponseDto(Long productId, int quantity, BigDecimal unitPrice) {}
}
