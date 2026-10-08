package com.dental.oms.order.client;

import com.dental.oms.order.client.DownstreamDtos.Product;
import com.dental.oms.order.web.DownstreamException;
import com.dental.oms.order.web.InvalidOrderException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CatalogClient {

    private final RestClient client;

    public CatalogClient(@Qualifier("catalogRestClient") RestClient client) {
        this.client = client;
    }

    public Product getProduct(String sku) {
        return client.get()
                .uri("/api/products/{sku}", sku)
                .retrieve()
                .onStatus(s -> s.value() == HttpStatus.NOT_FOUND.value(), (req, res) -> {
                    throw new InvalidOrderException("Unknown SKU: " + sku);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new DownstreamException("catalog-service", res.getStatusCode().value());
                })
                .body(Product.class);
    }
}
