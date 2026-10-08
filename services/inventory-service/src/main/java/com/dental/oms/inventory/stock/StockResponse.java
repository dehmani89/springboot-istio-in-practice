package com.dental.oms.inventory.stock;

public record StockResponse(String sku, String warehouseCode, int onHand, int reserved, int available,
                            boolean belowReorderLevel) {

    public static StockResponse from(StockItem s) {
        return new StockResponse(s.getSku(), s.getWarehouseCode(), s.getOnHand(), s.getReserved(),
                s.available(), s.available() <= s.getReorderLevel());
    }
}
