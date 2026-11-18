package com.sameer.sdl.system_design_labs.controller;

import com.sameer.sdl.system_design_labs.dto.UpdateProductRequest;
import com.sameer.sdl.system_design_labs.model.Product;
import com.sameer.sdl.system_design_labs.service.ProductService;
import com.sameer.sdl.system_design_labs.service.RateLimitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final RateLimitService rateLimitService;
    private final ProductService productService;

    public ProductController(RateLimitService rateLimitService, ProductService productService) {
        this.rateLimitService = rateLimitService;
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable("id") long id, @RequestHeader("X-Client-Id") String clientId) {
        log.info("getProduct: Checking rate limit for product {}.", id);
        if (clientId.isBlank()) {
            log.info("getProduct: Returning 400 because X-Client-Id is blank.");
            return ResponseEntity.badRequest().build();
        }
        var retryAfterSeconds = rateLimitService.checkLimit(clientId.strip());
        if (retryAfterSeconds > 0) {
            log.info("getProduct: Returning 429 for product {}. Retry in {} seconds.", id, retryAfterSeconds);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header(HttpHeaders.RETRY_AFTER, Long.toString(retryAfterSeconds))
                    .build();
        }
        log.info("getProduct: Rate limit passed for product {}. Looking up product.", id);

        return ResponseEntity.ofNullable(productService.findById(id));

    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateName(@PathVariable("id") long id, @RequestBody UpdateProductRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        var optProduct = productService.updateName(id, request.name());

        return optProduct.isPresent() ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
