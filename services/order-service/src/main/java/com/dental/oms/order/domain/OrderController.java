package com.dental.oms.order.domain;

import com.dental.oms.order.domain.OrderDtos.OrderResponse;
import com.dental.oms.order.domain.OrderDtos.PlaceOrderRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> place(@Valid @RequestBody PlaceOrderRequest request) {
        var order = service.placeOrder(request);
        return ResponseEntity.created(URI.create("/api/orders/" + order.orderNumber())).body(order);
    }

    @GetMapping("/{orderNumber}")
    public OrderResponse get(@PathVariable String orderNumber) {
        return service.get(orderNumber);
    }

    @GetMapping
    public List<OrderResponse> byCustomer(@RequestParam String customerId) {
        return service.forCustomer(customerId);
    }
}
