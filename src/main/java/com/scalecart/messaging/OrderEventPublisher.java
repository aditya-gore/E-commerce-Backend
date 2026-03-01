package com.scalecart.messaging;

public interface OrderEventPublisher {

    void publishOrderCreated(OrderCreatedEvent event);
}
