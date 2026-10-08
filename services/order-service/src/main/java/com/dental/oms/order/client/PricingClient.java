package com.dental.oms.order.client;

import com.dental.oms.order.client.DownstreamDtos.Quote;
import com.dental.oms.order.client.DownstreamDtos.QuoteRequest;
import com.dental.oms.order.web.DownstreamException;
import com.dental.oms.order.web.InvalidOrderException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PricingClient {

    private final RestClient client;

    public PricingClient(@Qualifier("pricingRestClient") RestClient client) {
        this.client = client;
    }

    public Quote quote(QuoteRequest request) {
        return client.post()
                .uri("/api/pricing/quotes")
                .body(request)
                .retrieve()
                .onStatus(s -> s.value() == 404, (req, res) -> {
                    throw new InvalidOrderException("One or more SKUs have no price");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new DownstreamException("pricing-service", res.getStatusCode().value());
                })
                .body(Quote.class);
    }
}
