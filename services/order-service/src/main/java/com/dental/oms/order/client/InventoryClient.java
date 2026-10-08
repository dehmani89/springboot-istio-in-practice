package com.dental.oms.order.client;

import com.dental.oms.order.client.DownstreamDtos.Reservation;
import com.dental.oms.order.client.DownstreamDtos.ReservationRequest;
import com.dental.oms.order.web.DownstreamException;
import com.dental.oms.order.web.OutOfStockException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class InventoryClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);

    private final RestClient client;
    private final ObjectMapper mapper;

    public InventoryClient(@Qualifier("inventoryRestClient") RestClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public Reservation reserve(ReservationRequest request) {
        return client.post()
                .uri("/api/inventory/reservations")
                .body(request)
                .retrieve()
                .onStatus(s -> s.value() == 409, (req, res) -> {
                    JsonNode body = mapper.readTree(res.getBody());
                    throw new OutOfStockException(body.path("shortages"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new DownstreamException("inventory-service", res.getStatusCode().value());
                })
                .body(Reservation.class);
    }

    /** Best-effort compensation. Failure is logged, not propagated. */
    public void release(String orderRef) {
        try {
            client.delete().uri("/api/inventory/reservations/{ref}", orderRef).retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Compensation failed: could not release reservation {} — manual cleanup required", orderRef, e);
        }
    }
}
