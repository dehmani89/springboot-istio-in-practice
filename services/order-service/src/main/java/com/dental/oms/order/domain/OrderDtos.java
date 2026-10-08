package com.dental.oms.order.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record PlaceOrderRequest(@NotBlank String customerId, @NotEmpty List<@Valid Item> items) {
    }

    public record Item(@NotBlank String sku, @Min(1) int quantity) {
    }

    public record LineResponse(String sku, String productName, int quantity, BigDecimal unitPrice,
                               BigDecimal discountPercent, BigDecimal lineTotal) {
    }

    public record OrderResponse(String orderNumber, String customerId, String status, String currency,
                                BigDecimal totalAmount, String pricingEngineVersion, Instant createdAt,
                                List<LineResponse> lines) {

        public static OrderResponse from(CustomerOrder o) {
            var lines = o.getLines().stream()
                    .map(l -> new LineResponse(l.getSku(), l.getProductName(), l.getQuantity(), l.getUnitPrice(),
                            l.getDiscountPercent(), l.getLineTotal()))
                    .toList();
            return new OrderResponse(o.getOrderNumber(), o.getCustomerId(), o.getStatus().name(), o.getCurrency(),
                    o.getTotalAmount(), o.getPricingEngineVersion(), o.getCreatedAt(), lines);
        }
    }
}
