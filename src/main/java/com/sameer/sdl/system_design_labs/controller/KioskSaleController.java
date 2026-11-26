package com.sameer.sdl.system_design_labs.controller;

import com.sameer.sdl.system_design_labs.service.KioskSaleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
public class KioskSaleController {

    private final KioskSaleService sales;

    public KioskSaleController(KioskSaleService sales) {
        this.sales = sales;
    }

    @PostMapping("/sales")
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse checkout(@RequestBody SaleRequest request) {
        var amount = request.amount();
        var receiptType = request.receiptType();

        var itemSkus = request.itemSkus();
        if (request.itemSku() != null) {
            if (itemSkus != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide itemSku or itemSkus.");
            }
            itemSkus = List.of(request.itemSku());
        }

        if (amount == null || amount.signum() <= 0
                || itemSkus == null || itemSkus.isEmpty()
                || itemSkus.stream().anyMatch(sku -> sku == null || sku.isBlank())
                || (!"EMAIL".equals(receiptType) && !"SMS".equals(receiptType))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provide a positive amount, at least one nonblank SKU, and EMAIL or SMS.");
        }

        return new SaleResponse(sales.completeSale(amount, itemSkus, receiptType));
    }

    public record SaleRequest(BigDecimal amount, String itemSku, String receiptType, List<String> itemSkus) {}

    public record SaleResponse(UUID saleId) {}

}
