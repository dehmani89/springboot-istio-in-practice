package com.dental.oms.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.List;

/**
 * Downstream endpoints. Locally these are localhost ports; in Kubernetes they are plain
 * Service names (http://catalog-service). No client-side discovery or load balancing —
 * Kubernetes DNS + the mesh handle that.
 *
 * Timeouts here are a coarse safety net only. Fine-grained timeouts/retries belong to the mesh
 * (HTTPRoute / DestinationRule) so they can change without a redeploy.
 */
@ConfigurationProperties(prefix = "oms.downstream")
public record DownstreamProperties(
        String catalogUrl,
        String inventoryUrl,
        String pricingUrl,
        @DefaultValue("2s") Duration connectTimeout,
        @DefaultValue("5s") Duration readTimeout,
        @DefaultValue({"x-request-id", "x-pricing-version", "x-customer-tier"}) List<String> propagateHeaders) {
}
