package com.dental.oms.order.domain;

import com.dental.oms.order.client.CatalogClient;
import com.dental.oms.order.client.DownstreamDtos;
import com.dental.oms.order.client.DownstreamDtos.Product;
import com.dental.oms.order.client.DownstreamDtos.QuoteLine;
import com.dental.oms.order.client.InventoryClient;
import com.dental.oms.order.client.PricingClient;
import com.dental.oms.order.domain.OrderDtos.OrderResponse;
import com.dental.oms.order.domain.OrderDtos.PlaceOrderRequest;
import com.dental.oms.order.web.InvalidOrderException;
import com.dental.oms.order.web.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestrates: validate (catalog) -> quote (pricing) -> reserve (inventory) -> persist.
 * Remote calls happen outside any DB transaction. If persistence fails after a successful
 * reservation, the reservation is compensated (released).
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final CatalogClient catalog;
    private final PricingClient pricing;
    private final InventoryClient inventory;
    private final OrderRepository repository;

    public OrderService(CatalogClient catalog, PricingClient pricing, InventoryClient inventory,
                        OrderRepository repository) {
        this.catalog = catalog;
        this.pricing = pricing;
        this.inventory = inventory;
        this.repository = repository;
    }

    public OrderResponse placeOrder(PlaceOrderRequest request) {
        // Merge duplicate SKUs, preserve caller order.
        Map<String, Integer> merged = new LinkedHashMap<>();
        request.items().forEach(i -> merged.merge(i.sku(), i.quantity(), Integer::sum));
        List<DownstreamDtos.Item> items = merged.entrySet().stream()
                .map(e -> new DownstreamDtos.Item(e.getKey(), e.getValue())).toList();

        // 1. Validate against catalog
        Map<String, Product> products = items.stream()
                .map(i -> catalog.getProduct(i.sku()))
                .collect(Collectors.toMap(Product::sku, Function.identity()));
        products.values().stream().filter(p -> !p.active()).findFirst().ifPresent(p -> {
            throw new InvalidOrderException("Product is no longer available: " + p.sku());
        });

        // 2. Price
        var quote = pricing.quote(new DownstreamDtos.QuoteRequest(items));
        Map<String, QuoteLine> quoted = quote.lines().stream()
                .collect(Collectors.toMap(QuoteLine::sku, Function.identity()));

        // 3. Reserve stock (orderNumber doubles as the idempotency key)
        String orderNumber = UUID.randomUUID().toString();
        inventory.reserve(new DownstreamDtos.ReservationRequest(orderNumber, items));

        // 4. Persist, compensating on failure
        try {
            var order = new CustomerOrder(orderNumber, request.customerId(), quote.currency(), quote.total(),
                    quote.engineVersion());
            for (var item : items) {
                var q = quoted.get(item.sku());
                order.addLine(new OrderLine(item.sku(), products.get(item.sku()).name(), item.quantity(),
                        q.unitPrice(), q.discountPercent(), q.lineTotal()));
            }
            var saved = repository.save(order);
            log.info("Order {} confirmed for {} (pricing engine {}), total {} {}", orderNumber,
                    request.customerId(), quote.engineVersion(), quote.total(), quote.currency());
            return OrderResponse.from(saved);
        } catch (RuntimeException e) {
            log.error("Persisting order {} failed — releasing reservation", orderNumber, e);
            inventory.release(orderNumber);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public OrderResponse get(String orderNumber) {
        return repository.findByOrderNumber(orderNumber).map(OrderResponse::from)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderNumber));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> forCustomer(String customerId) {
        return repository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(OrderResponse::from).toList();
    }
}
