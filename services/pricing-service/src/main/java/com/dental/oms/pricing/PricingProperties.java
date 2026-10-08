package com.dental.oms.pricing;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * pricing.engine-version (env PRICING_ENGINE_VERSION):
 *   v1 = list price only
 *   v2 = list price + volume discount tiers
 * Same image, two Deployments — this is the canary target for Istio traffic splitting.
 */
@ConfigurationProperties(prefix = "pricing")
public record PricingProperties(@DefaultValue("v1") String engineVersion) {

    public boolean volumeDiscountsEnabled() {
        return "v2".equalsIgnoreCase(engineVersion);
    }
}
