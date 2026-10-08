package com.dental.oms.pricing.chaos;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * App-level fault injection for resilience labs.
 *   chaos.latency-ms   (env CHAOS_LATENCY_MS)    fixed delay added to every /api/** request
 *   chaos.failure-rate (env CHAOS_FAILURE_RATE)  0.0–1.0 probability of returning 503
 * Toggle per Deployment with:  kubectl set env deploy/pricing-service-v2 CHAOS_FAILURE_RATE=0.5
 */
@ConfigurationProperties(prefix = "chaos")
public record ChaosProperties(@DefaultValue("0") long latencyMs, @DefaultValue("0.0") double failureRate) {

    public boolean active() {
        return latencyMs > 0 || failureRate > 0;
    }
}
