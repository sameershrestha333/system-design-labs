package com.sameer.sdl.system_design_labs.controller;

import com.sameer.sdl.system_design_labs.dto.UpdateProductRequest;
import com.sameer.sdl.system_design_labs.model.Product;
import com.sameer.sdl.system_design_labs.service.ProductService;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable("id") long id) {
        return ResponseEntity.ofNullable(productService.findById(id));

    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateName(@PathVariable("id") long id, @RequestBody UpdateProductRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        boolean updated = productService.updateName(id, request.name());

        return updated ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
