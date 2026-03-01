package com.scalecart.service;

import com.scalecart.domain.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OrderConfirmationService {

    private static final Logger log = LoggerFactory.getLogger(OrderConfirmationService.class);

    @Async("orderConfirmationExecutor")
    public void confirmOrder(Order order) {
        log.info("Order confirmation (async): orderId={}, customerId={}, total={}",
            order.getId(), order.getCustomerId(), order.getTotalAmount());
    }
}
