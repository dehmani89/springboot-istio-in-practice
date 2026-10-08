package com.dental.oms.pricing.quote;

import com.dental.oms.pricing.PricingProperties;
import com.dental.oms.pricing.price.PriceListEntryRepository;
import com.dental.oms.pricing.price.VolumeDiscountTierRepository;
import com.dental.oms.pricing.quote.QuoteDtos.QuoteLine;
import com.dental.oms.pricing.quote.QuoteDtos.QuoteRequest;
import com.dental.oms.pricing.quote.QuoteDtos.QuoteResponse;
import com.dental.oms.pricing.web.UnknownSkuException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class QuoteService {

    private final PriceListEntryRepository prices;
    private final VolumeDiscountTierRepository tiers;
    private final PricingProperties properties;

    public QuoteService(PriceListEntryRepository prices, VolumeDiscountTierRepository tiers,
                        PricingProperties properties) {
        this.prices = prices;
        this.tiers = tiers;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public QuoteResponse quote(QuoteRequest request) {
        var allTiers = tiers.findAll();
        boolean discounts = properties.volumeDiscountsEnabled();

        var lines = request.items().stream().map(item -> {
            var entry = prices.findById(item.sku()).orElseThrow(() -> new UnknownSkuException(item.sku()));
            var pct = DiscountCalculator.discountPercentFor(item.quantity(), allTiers, discounts);
            var total = DiscountCalculator.lineTotal(entry.getListPrice(), item.quantity(), pct);
            return new QuoteLine(item.sku(), item.quantity(), entry.getListPrice(), pct, total);
        }).toList();

        var grandTotal = lines.stream().map(QuoteLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new QuoteResponse(properties.engineVersion(), "USD", lines, grandTotal);
    }
}
