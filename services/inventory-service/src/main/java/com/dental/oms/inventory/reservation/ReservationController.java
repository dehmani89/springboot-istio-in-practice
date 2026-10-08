package com.dental.oms.inventory.reservation;

import com.dental.oms.inventory.reservation.ReservationDtos.ReservationRequest;
import com.dental.oms.inventory.reservation.ReservationDtos.ReservationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(@Valid @RequestBody ReservationRequest request) {
        var response = service.reserve(request);
        return ResponseEntity.status(response.replayed() ? HttpStatus.OK : HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{orderRef}")
    public ResponseEntity<Void> release(@PathVariable String orderRef) {
        service.release(orderRef);
        return ResponseEntity.noContent().build();
    }
}
