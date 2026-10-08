package com.dental.oms.inventory.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_ref", nullable = false, length = 64)
    private String orderRef;

    @Column(nullable = false, length = 32)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    protected Reservation() {
    }

    public Reservation(String orderRef, String sku, int quantity) {
        this.orderRef = orderRef;
        this.sku = sku;
        this.quantity = quantity;
        this.status = ReservationStatus.RESERVED;
        this.createdAt = Instant.now();
    }

    public void markReleased() {
        this.status = ReservationStatus.RELEASED;
        this.releasedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getOrderRef() { return orderRef; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
    public ReservationStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReleasedAt() { return releasedAt; }
}
