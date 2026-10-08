package com.dental.oms.inventory.reservation;

import java.util.List;

public class InsufficientStockException extends RuntimeException {

    private final List<ReservationDtos.Shortage> shortages;

    public InsufficientStockException(List<ReservationDtos.Shortage> shortages) {
        super("Insufficient stock for " + shortages.size() + " item(s)");
        this.shortages = shortages;
    }

    public List<ReservationDtos.Shortage> getShortages() {
        return shortages;
    }
}
