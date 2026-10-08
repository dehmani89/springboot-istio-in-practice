package com.dental.oms.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "order_line")
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @Column(nullable = false, length = 32)
    private String sku;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    protected OrderLine() {
    }

    public OrderLine(String sku, String productName, int quantity, BigDecimal unitPrice,
                     BigDecimal discountPercent, BigDecimal lineTotal) {
        this.sku = sku;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountPercent = discountPercent;
        this.lineTotal = lineTotal;
    }

    void attachTo(CustomerOrder order) {
        this.order = order;
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
