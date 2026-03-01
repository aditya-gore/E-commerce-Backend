package com.scalecart.web;

import com.scalecart.domain.Order;
import com.scalecart.dto.OrderCreateDto;
import com.scalecart.dto.OrderResponseDto;
import com.scalecart.exception.NotFoundException;
import com.scalecart.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/orders")
@Tag(name = "Orders", description = "Order API")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponseDto> getById(@PathVariable Long id) {
        return orderService.findByIdWithItems(id)
            .map(OrderController::toResponse)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new NotFoundException("Order", id));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List orders by customer ID")
    public List<OrderResponseDto> listByCustomer(@RequestParam Long customerId) {
        return orderService.findByCustomerId(customerId).stream()
            .map(OrderController::toResponse)
            .toList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create order")
    public ResponseEntity<OrderResponseDto> create(@Valid @RequestBody OrderCreateDto dto) {
        var items = dto.items().stream()
            .map(i -> new OrderService.OrderItemRequest(i.productId(), i.quantity()))
            .toList();
        Order order = orderService.createOrder(dto.customerId(), items);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(order));
    }

    static OrderResponseDto toResponse(Order o) {
        var items = o.getItems().stream()
            .map(i -> new OrderResponseDto.OrderItemResponseDto(
                i.getProduct().getId(),
                i.getQuantity(),
                i.getUnitPrice()
            ))
            .toList();
        return new OrderResponseDto(
            o.getId(),
            o.getCustomerId(),
            o.getStatus().name(),
            o.getTotalAmount(),
            o.getCreatedAt(),
            items
        );
    }
}
