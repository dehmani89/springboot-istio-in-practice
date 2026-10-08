package com.dental.oms.inventory.stock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "stock_item")
public class StockItem {

    @Id
    @Column(length = 32)
    private String sku;

    @Column(name = "warehouse_code", nullable = false, length = 20)
    private String warehouseCode;

    @Column(name = "on_hand", nullable = false)
    private int onHand;

    @Column(nullable = false)
    private int reserved;

    @Column(name = "reorder_level", nullable = false)
    private int reorderLevel;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected StockItem() {
    }

    public int available() {
        return onHand - reserved;
    }

    public void reserve(int quantity) {
        if (quantity > available()) {
            throw new IllegalStateException("Insufficient stock for " + sku);
        }
        reserved += quantity;
        updatedAt = Instant.now();
    }

    public void release(int quantity) {
        reserved = Math.max(0, reserved - quantity);
        updatedAt = Instant.now();
    }

    public String getSku() { return sku; }
    public String getWarehouseCode() { return warehouseCode; }
    public int getOnHand() { return onHand; }
    public int getReserved() { return reserved; }
    public int getReorderLevel() { return reorderLevel; }
    public Instant getUpdatedAt() { return updatedAt; }
}
