package com.dental.oms.pricing.quote;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

public final class QuoteDtos {

    private QuoteDtos() {
    }

    public record QuoteRequest(@NotEmpty List<@Valid Item> items) {
    }

    public record Item(@NotBlank String sku, @Min(1) int quantity) {
    }

    public record QuoteLine(String sku, int quantity, BigDecimal unitPrice, BigDecimal discountPercent,
                            BigDecimal lineTotal) {
    }

    public record QuoteResponse(String engineVersion, String currency, List<QuoteLine> lines, BigDecimal total) {
    }

    public record PriceResponse(String sku, BigDecimal listPrice, String currency, String engineVersion) {
    }
}
