package com.dental.oms.pricing.quote;

import com.dental.oms.pricing.price.VolumeDiscountTier;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountCalculatorTest {

    private final List<VolumeDiscountTier> tiers = List.of(
            new VolumeDiscountTier(10, new BigDecimal("5.00")),
            new VolumeDiscountTier(25, new BigDecimal("10.00")),
            new VolumeDiscountTier(50, new BigDecimal("15.00")));

    @Test
    void v1_never_discounts() {
        assertThat(DiscountCalculator.discountPercentFor(100, tiers, false)).isEqualByComparingTo("0");
    }

    @Test
    void v2_picks_highest_qualifying_tier() {
        assertThat(DiscountCalculator.discountPercentFor(9, tiers, true)).isEqualByComparingTo("0");
        assertThat(DiscountCalculator.discountPercentFor(10, tiers, true)).isEqualByComparingTo("5");
        assertThat(DiscountCalculator.discountPercentFor(30, tiers, true)).isEqualByComparingTo("10");
        assertThat(DiscountCalculator.discountPercentFor(75, tiers, true)).isEqualByComparingTo("15");
    }

    @Test
    void line_total_applies_discount_and_rounds_half_up() {
        // 12 x 14.99 = 179.88, less 5% = 170.886 -> 170.89
        assertThat(DiscountCalculator.lineTotal(new BigDecimal("14.99"), 12, new BigDecimal("5")))
                .isEqualByComparingTo("170.89");
    }
}
