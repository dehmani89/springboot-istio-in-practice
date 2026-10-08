package com.dental.oms.pricing.quote;

import com.dental.oms.pricing.price.VolumeDiscountTier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

/** Pure pricing math — no Spring, no I/O — so it is trivially unit-testable. */
public final class DiscountCalculator {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private DiscountCalculator() {
    }

    public static BigDecimal discountPercentFor(int quantity, List<VolumeDiscountTier> tiers, boolean enabled) {
        if (!enabled) {
            return BigDecimal.ZERO;
        }
        return tiers.stream()
                .filter(t -> quantity >= t.getMinQuantity())
                .max(Comparator.comparingInt(VolumeDiscountTier::getMinQuantity))
                .map(VolumeDiscountTier::getDiscountPercent)
                .orElse(BigDecimal.ZERO);
    }

    public static BigDecimal lineTotal(BigDecimal unitPrice, int quantity, BigDecimal discountPercent) {
        var gross = unitPrice.multiply(BigDecimal.valueOf(quantity));
        var factor = HUNDRED.subtract(discountPercent).divide(HUNDRED, 6, RoundingMode.HALF_UP);
        return gross.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
