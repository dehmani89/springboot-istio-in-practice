package com.dental.oms.order.client;

import java.math.BigDecimal;
import java.util.List;

public final class DownstreamDtos {

    private DownstreamDtos() {
    }

    // catalog-service
    public record Product(String sku, String name, String category, String unitOfMeasure, boolean active) {
    }

    // shared line item shape for pricing + inventory
    public record Item(String sku, int quantity) {
    }

    // pricing-service
    public record QuoteRequest(List<Item> items) {
    }

    public record QuoteLine(String sku, int quantity, BigDecimal unitPrice, BigDecimal discountPercent,
                            BigDecimal lineTotal) {
    }

    public record Quote(String engineVersion, String currency, List<QuoteLine> lines, BigDecimal total) {
    }

    // inventory-service
    public record ReservationRequest(String orderRef, List<Item> items) {
    }

    public record Reservation(String orderRef, String status, List<Item> items, boolean replayed) {
    }
}
