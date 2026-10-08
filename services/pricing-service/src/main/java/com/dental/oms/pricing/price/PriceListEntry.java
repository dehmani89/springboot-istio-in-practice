package com.dental.oms.pricing.price;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "price_list_entry")
public class PriceListEntry {

    @Id
    @Column(length = 32)
    private String sku;

    @Column(name = "list_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal listPrice;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PriceListEntry() {
    }

    public String getSku() { return sku; }
    public BigDecimal getListPrice() { return listPrice; }
    public String getCurrency() { return currency; }
    public Instant getUpdatedAt() { return updatedAt; }
}
