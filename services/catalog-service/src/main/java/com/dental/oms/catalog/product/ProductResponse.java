package com.dental.oms.catalog.product;

public record ProductResponse(
        String sku,
        String name,
        String category,
        String manufacturer,
        String description,
        String unitOfMeasure,
        boolean active) {

    static ProductResponse from(Product p) {
        return new ProductResponse(p.getSku(), p.getName(), p.getCategory(), p.getManufacturer(),
                p.getDescription(), p.getUnitOfMeasure(), p.isActive());
    }
}
