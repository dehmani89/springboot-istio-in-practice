package com.dental.oms.pricing.web;

public class UnknownSkuException extends RuntimeException {

    public UnknownSkuException(String sku) {
        super("No price list entry for " + sku);
    }
}
