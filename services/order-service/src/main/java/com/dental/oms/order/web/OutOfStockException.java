package com.dental.oms.order.web;

import com.fasterxml.jackson.databind.JsonNode;

public class OutOfStockException extends RuntimeException {

    private final transient JsonNode shortages;

    public OutOfStockException(JsonNode shortages) {
        super("Insufficient stock");
        this.shortages = shortages;
    }

    public JsonNode getShortages() {
        return shortages;
    }
}
