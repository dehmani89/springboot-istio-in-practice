package com.dental.oms.order.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_order")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 64)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false, length = 40)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "pricing_engine_version", nullable = false, length = 10)
    private String pricingEngineVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderLine> lines = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(String orderNumber, String customerId, String currency, BigDecimal totalAmount,
                         String pricingEngineVersion) {
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.currency = currency;
        this.totalAmount = totalAmount;
        this.pricingEngineVersion = pricingEngineVersion;
        this.status = OrderStatus.CONFIRMED;
        this.createdAt = Instant.now();
    }

    public void addLine(OrderLine line) {
        line.attachTo(this);
        lines.add(line);
    }

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public String getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
    public String getCurrency() { return currency; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPricingEngineVersion() { return pricingEngineVersion; }
    public Instant getCreatedAt() { return createdAt; }
    public List<OrderLine> getLines() { return lines; }
}
