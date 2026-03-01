package com.scalecart.service;

import com.scalecart.domain.Order;
import com.scalecart.domain.Order.OrderStatus;
import com.scalecart.domain.OrderItem;
import com.scalecart.domain.Product;
import com.scalecart.messaging.OrderCreatedEvent;
import com.scalecart.messaging.OrderEventPublisher;
import com.scalecart.repository.OrderRepository;
import com.scalecart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final OrderConfirmationService orderConfirmationService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        OrderEventPublisher orderEventPublisher,
                        OrderConfirmationService orderConfirmationService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderEventPublisher = orderEventPublisher;
        this.orderConfirmationService = orderConfirmationService;
    }

    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Order> findByIdWithItems(Long id) {
        return orderRepository.findByIdWithItems(id);
    }

    @Transactional(readOnly = true)
    public List<Order> findByCustomerId(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional
    public Order createOrder(Long customerId, List<OrderItemRequest> lineItems) {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setStatus(OrderStatus.PENDING);
        for (OrderItemRequest req : lineItems) {
            Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + req.productId()));
            if (product.getStockQuantity() < req.quantity()) {
                throw new IllegalArgumentException("Insufficient stock for product " + product.getSku());
            }
            product.setStockQuantity(product.getStockQuantity() - req.quantity());
            new OrderItem(order, product, req.quantity(), product.getPrice());
        }
        Order saved = orderRepository.save(order);
        orderEventPublisher.publishOrderCreated(
            OrderCreatedEvent.v1(saved.getId(), saved.getCustomerId(), saved.getTotalAmount()));
        orderConfirmationService.confirmOrder(saved);
        return saved;
    }

    public record OrderItemRequest(long productId, int quantity) {}
}
