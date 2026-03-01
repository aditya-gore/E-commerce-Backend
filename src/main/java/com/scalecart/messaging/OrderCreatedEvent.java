package com.scalecart.messaging;

import java.math.BigDecimal;

public record OrderCreatedEvent(
    long orderId,
    long customerId,
    BigDecimal totalAmount,
    String version
) {
    public static final String V1 = "v1";

    public static OrderCreatedEvent v1(long orderId, long customerId, BigDecimal totalAmount) {
        return new OrderCreatedEvent(orderId, customerId, totalAmount, V1);
    }
}
