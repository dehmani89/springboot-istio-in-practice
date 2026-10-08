package com.dental.oms.inventory.reservation;

import com.dental.oms.inventory.reservation.ReservationDtos.Item;
import com.dental.oms.inventory.reservation.ReservationDtos.ReservationRequest;
import com.dental.oms.inventory.reservation.ReservationDtos.ReservationResponse;
import com.dental.oms.inventory.reservation.ReservationDtos.Shortage;
import com.dental.oms.inventory.stock.StockItem;
import com.dental.oms.inventory.stock.StockItemRepository;
import com.dental.oms.inventory.web.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final StockItemRepository stockRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(StockItemRepository stockRepository, ReservationRepository reservationRepository) {
        this.stockRepository = stockRepository;
        this.reservationRepository = reservationRepository;
    }

    /**
     * All-or-nothing reservation. Idempotent on orderRef: the mesh may retry this call
     * (Istio retries on 503 / connect failures), so a replay must not double-reserve.
     */
    @Transactional
    public ReservationResponse reserve(ReservationRequest request) {
        var existing = reservationRepository.findByOrderRefAndStatus(request.orderRef(), ReservationStatus.RESERVED);
        if (!existing.isEmpty()) {
            log.info("Replayed reservation for orderRef={} — returning existing result", request.orderRef());
            return toResponse(request.orderRef(), existing, true);
        }

        // Merge duplicate SKUs and lock rows in a stable (sorted) order to avoid deadlocks.
        Map<String, Integer> merged = new TreeMap<>();
        request.items().forEach(i -> merged.merge(i.sku(), i.quantity(), Integer::sum));

        List<StockItem> locked = new ArrayList<>();
        List<Shortage> shortages = new ArrayList<>();
        for (var entry : merged.entrySet()) {
            var stock = stockRepository.findForUpdate(entry.getKey())
                    .orElseThrow(() -> new NotFoundException("No stock record for " + entry.getKey()));
            if (stock.available() < entry.getValue()) {
                shortages.add(new Shortage(entry.getKey(), entry.getValue(), stock.available()));
            }
            locked.add(stock);
        }
        if (!shortages.isEmpty()) {
            throw new InsufficientStockException(shortages);
        }

        List<Reservation> created = new ArrayList<>();
        for (var stock : locked) {
            int qty = merged.get(stock.getSku());
            stock.reserve(qty);
            created.add(reservationRepository.save(new Reservation(request.orderRef(), stock.getSku(), qty)));
        }
        log.info("Reserved {} line(s) for orderRef={}", created.size(), request.orderRef());
        return toResponse(request.orderRef(), created, false);
    }

    /** Compensation path used by order-service when an order fails after reservation. Idempotent. */
    @Transactional
    public void release(String orderRef) {
        var active = reservationRepository.findByOrderRefAndStatus(orderRef, ReservationStatus.RESERVED);
        active.sort(Comparator.comparing(Reservation::getSku));
        for (var r : active) {
            stockRepository.findForUpdate(r.getSku()).ifPresent(s -> s.release(r.getQuantity()));
            r.markReleased();
        }
        log.info("Released {} reservation line(s) for orderRef={}", active.size(), orderRef);
    }

    private ReservationResponse toResponse(String orderRef, List<Reservation> reservations, boolean replayed) {
        var items = reservations.stream().map(r -> new Item(r.getSku(), r.getQuantity())).toList();
        return new ReservationResponse(orderRef, ReservationStatus.RESERVED.name(), items, replayed);
    }
}
