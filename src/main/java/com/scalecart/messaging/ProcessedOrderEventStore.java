package com.scalecart.messaging;

public interface ProcessedOrderEventStore {

    boolean alreadyProcessed(long orderId);

    void markProcessed(long orderId);
}
