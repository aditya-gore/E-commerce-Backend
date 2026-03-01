package com.scalecart.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final ProcessedOrderEventStore processedStore;

    public OrderEventConsumer(ProcessedOrderEventStore processedStore) {
        this.processedStore = processedStore;
    }

    @KafkaListener(topics = KafkaOrderEventPublisher.TOPIC, groupId = "scalecart-capstone")
    public void onOrderCreated(String message, String key) {
        long orderId = Long.parseLong(key);
        if (processedStore.alreadyProcessed(orderId)) {
            log.debug("Order event already processed (idempotent skip): orderId={}", orderId);
            return;
        }
        log.info("Consumed order created event: orderId={}, payload={}", orderId, message);
        processedStore.markProcessed(orderId);
    }
}
