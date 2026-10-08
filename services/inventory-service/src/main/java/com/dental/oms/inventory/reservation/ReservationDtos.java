package com.dental.oms.inventory.reservation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public final class ReservationDtos {

    private ReservationDtos() {
    }

    public record ReservationRequest(@NotBlank String orderRef, @NotEmpty List<@Valid Item> items) {
    }

    public record Item(@NotBlank String sku, @Min(1) int quantity) {
    }

    public record ReservationResponse(String orderRef, String status, List<Item> items, boolean replayed) {
    }

    public record Shortage(String sku, int requested, int available) {
    }
}
