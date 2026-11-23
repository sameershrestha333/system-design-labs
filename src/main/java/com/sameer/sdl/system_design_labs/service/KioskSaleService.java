package com.sameer.sdl.system_design_labs.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class KioskSaleService {

    private final JdbcTemplate jdbc;

    public KioskSaleService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public UUID completeSale(BigDecimal amount, String itemSku, String receiptType) {
        var saleId = UUID.randomUUID();
        var eventId = UUID.randomUUID();

        jdbc.update("""
                INSERT INTO kiosk_sales (sale_id, amount, item_sku, receipt_type)
                VALUES (?, ?, ?, ?)
                """, saleId, amount, itemSku, receiptType);

        jdbc.update("""
                INSERT INTO kiosk_sale_outbox (event_id, sale_id, payload)
                SELECT evt.event_id, sale.sale_id, jsonb_build_object(
                    'eventId', evt.event_id,
                    'eventType', 'SaleCompleted',
                    'saleId', sale.sale_id,
                    'amount', sale.amount,
                    'itemSku', sale.item_sku,
                    'receiptType', sale.receipt_type
                )
                FROM kiosk_sales sale
                CROSS JOIN (SELECT CAST(? AS uuid) AS event_id) evt
                WHERE sale.sale_id = ?
                """, eventId, saleId);

        return saleId;
    }
}
