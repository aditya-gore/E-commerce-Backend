package com.scalecart.messaging;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryProcessedOrderEventStore implements ProcessedOrderEventStore {

    private final ConcurrentHashMap<Long, Boolean> processed = new ConcurrentHashMap<>();

    @Override
    public boolean alreadyProcessed(long orderId) {
        return processed.containsKey(orderId);
    }

    @Override
    public void markProcessed(long orderId) {
        processed.put(orderId, Boolean.TRUE);
    }
}
