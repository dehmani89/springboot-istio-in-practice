package com.dental.oms.pricing.quote;

import com.dental.oms.pricing.PricingProperties;
import com.dental.oms.pricing.price.PriceListEntryRepository;
import com.dental.oms.pricing.quote.QuoteDtos.PriceResponse;
import com.dental.oms.pricing.quote.QuoteDtos.QuoteRequest;
import com.dental.oms.pricing.quote.QuoteDtos.QuoteResponse;
import com.dental.oms.pricing.web.UnknownSkuException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    /** Response header that makes the serving version visible in curl output and access logs. */
    public static final String ENGINE_HEADER = "x-pricing-engine";

    private final QuoteService quoteService;
    private final PriceListEntryRepository prices;
    private final PricingProperties properties;

    public PricingController(QuoteService quoteService, PriceListEntryRepository prices,
                             PricingProperties properties) {
        this.quoteService = quoteService;
        this.prices = prices;
        this.properties = properties;
    }

    @PostMapping("/quotes")
    public ResponseEntity<QuoteResponse> quote(@Valid @RequestBody QuoteRequest request) {
        return ResponseEntity.ok()
                .header(ENGINE_HEADER, properties.engineVersion())
                .body(quoteService.quote(request));
    }

    @GetMapping("/{sku}")
    public ResponseEntity<PriceResponse> price(@PathVariable String sku) {
        var entry = prices.findById(sku).orElseThrow(() -> new UnknownSkuException(sku));
        return ResponseEntity.ok()
                .header(ENGINE_HEADER, properties.engineVersion())
                .body(new PriceResponse(sku, entry.getListPrice(), entry.getCurrency(), properties.engineVersion()));
    }
}
