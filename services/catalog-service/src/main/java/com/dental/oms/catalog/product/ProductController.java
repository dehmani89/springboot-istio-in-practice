package com.dental.oms.catalog.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ProductResponse> list(@RequestParam(required = false) String category) {
        var products = (category == null || category.isBlank())
                ? repository.findAllByOrderByCategoryAscNameAsc()
                : repository.findByCategoryIgnoreCaseOrderByNameAsc(category);
        return products.stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/{sku}")
    public ProductResponse get(@PathVariable String sku) {
        return repository.findBySku(sku)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }
}
