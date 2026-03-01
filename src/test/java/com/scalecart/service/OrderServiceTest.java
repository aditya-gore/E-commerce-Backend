package com.scalecart.service;

import com.scalecart.domain.Order;
import com.scalecart.domain.Order.OrderStatus;
import com.scalecart.domain.Product;
import com.scalecart.messaging.OrderEventPublisher;
import com.scalecart.repository.OrderRepository;
import com.scalecart.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderEventPublisher orderEventPublisher;
    @Mock
    private OrderConfirmationService orderConfirmationService;
    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_throwsWhenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.createOrder(10L, List.of(new OrderService.OrderItemRequest(1L, 1))))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Product not found");
    }

    @Test
    void createOrder_throwsWhenInsufficientStock() {
        Product p = product(1L, 0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        assertThatThrownBy(() -> orderService.createOrder(10L, List.of(new OrderService.OrderItemRequest(1L, 1))))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Insufficient stock");
    }

    @Test
    void createOrder_savesOrderPublishesEventAndConfirms() {
        Product p = product(1L, 10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        Order saved = new Order();
        saved.setId(1L);
        saved.setCustomerId(10L);
        saved.setStatus(OrderStatus.PENDING);
        saved.setTotalAmount(BigDecimal.TEN);
        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        Order result = orderService.createOrder(10L, List.of(new OrderService.OrderItemRequest(1L, 2)));

        assertThat(result).isSameAs(saved);
        assertThat(p.getStockQuantity()).isEqualTo(8);
        verify(orderRepository).save(any(Order.class));
        verify(orderEventPublisher).publishOrderCreated(any());
        verify(orderConfirmationService).confirmOrder(saved);
    }

    @Test
    void findById_returnsEmptyWhenNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThat(orderService.findById(1L)).isEmpty();
    }

    private static Product product(long id, int stock) {
        Product p = new Product();
        p.setId(id);
        p.setSku("SKU");
        p.setPrice(BigDecimal.TEN);
        p.setStockQuantity(stock);
        return p;
    }
}
