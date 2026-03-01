package com.scalecart.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpOrderEventPublisher implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpOrderEventPublisher.class);

    @Override
    public void publishOrderCreated(OrderCreatedEvent event) {
        log.debug("Kafka disabled: would publish order created orderId={}", event.orderId());
    }
}
