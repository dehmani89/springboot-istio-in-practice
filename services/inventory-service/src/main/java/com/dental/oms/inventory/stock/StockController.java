package com.dental.oms.inventory.stock;

import com.dental.oms.inventory.web.NotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class StockController {

    private final StockItemRepository repository;

    public StockController(StockItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<StockResponse> list() {
        return repository.findAll(Sort.by("sku")).stream().map(StockResponse::from).toList();
    }

    @GetMapping("/{sku}")
    public StockResponse get(@PathVariable String sku) {
        return repository.findById(sku).map(StockResponse::from)
                .orElseThrow(() -> new NotFoundException("No stock record for " + sku));
    }
}
