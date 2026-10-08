package com.dental.oms.pricing.price;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "volume_discount_tier")
public class VolumeDiscountTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "min_quantity", nullable = false, unique = true)
    private int minQuantity;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    protected VolumeDiscountTier() {
    }

    public VolumeDiscountTier(int minQuantity, BigDecimal discountPercent) {
        this.minQuantity = minQuantity;
        this.discountPercent = discountPercent;
    }

    public Long getId() { return id; }
    public int getMinQuantity() { return minQuantity; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
}
