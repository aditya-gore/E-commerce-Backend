package com.scalecart.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaOrderEventPublisher implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaOrderEventPublisher.class);
    public static final String TOPIC = "order-events";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaOrderEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishOrderCreated(OrderCreatedEvent event) {
        String key = String.valueOf(event.orderId());
        String payload = event.orderId() + "," + event.customerId() + "," + event.totalAmount() + "," + event.version();
        kafkaTemplate.send(TOPIC, key, payload);
        log.debug("Published order created event: orderId={}", event.orderId());
    }
}
