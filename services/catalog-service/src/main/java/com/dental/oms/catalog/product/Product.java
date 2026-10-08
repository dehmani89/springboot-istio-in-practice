package com.dental.oms.catalog.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 100)
    private String manufacturer;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "unit_of_measure", nullable = false, length = 30)
    private String unitOfMeasure;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    protected Product() {
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getManufacturer() { return manufacturer; }
    public String getDescription() { return description; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}
