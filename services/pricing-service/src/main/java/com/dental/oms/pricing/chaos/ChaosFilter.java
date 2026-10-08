package com.dental.oms.pricing.chaos;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ChaosFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ChaosFilter.class);

    private final ChaosProperties chaos;

    public ChaosFilter(ChaosProperties chaos) {
        this.chaos = chaos;
        if (chaos.active()) {
            log.warn("CHAOS ENABLED: latencyMs={}, failureRate={}", chaos.latencyMs(), chaos.failureRate());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !chaos.active() || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (chaos.latencyMs() > 0) {
            try {
                Thread.sleep(chaos.latencyMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (ThreadLocalRandom.current().nextDouble() < chaos.failureRate()) {
            log.warn("Chaos: injecting 503 for {} {}", request.getMethod(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType("application/problem+json");
            response.getWriter().write("{\"status\":503,\"title\":\"Service Unavailable\",\"detail\":\"chaos-injected failure\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
