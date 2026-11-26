package com.sameer.sdl.system_design_labs.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class KioskSaleService {

    private final JdbcTemplate jdbc;

    public KioskSaleService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public UUID completeSale(BigDecimal amount, List<String> itemSkus, String receiptType) {
        var saleId = UUID.randomUUID();
        var eventId = UUID.randomUUID();

        jdbc.update("""
                INSERT INTO kskdb.salesheader (sale_id, amount, receipt_type)
                VALUES (?, ?, ?)
                """, saleId, amount, receiptType);

        // Prepare one parameter set per item, then submit them together in one JDBC batch.
        var rows = new ArrayList<Object[]>(itemSkus.size());
        for (int i = 0; i < itemSkus.size(); i++) {
            rows.add(new Object[] {saleId, i + 1, itemSkus.get(i)});
        }
        jdbc.batchUpdate("""
                INSERT INTO kskdb.salesitem (sale_id, line_no, item_sku)
                VALUES (?, ?, ?)
                """, rows);

        // Record one event for the whole sale in the same transaction as its items.
        jdbc.update("""
                INSERT INTO kskdb.kiosk_sale_outbox (event_id, sale_id, payload)
                SELECT evt.event_id, sale.sale_id, jsonb_build_object(
                    'eventId', evt.event_id,
                    'eventType', 'SaleCompleted',
                    'saleId', sale.sale_id,
                    'amount', sale.amount,
                    'itemSkus', (
                        SELECT jsonb_agg(item_sku ORDER BY line_no)
                        FROM kskdb.salesitem WHERE sale_id = sale.sale_id
                    ),
                    'receiptType', sale.receipt_type
                )
                FROM kskdb.salesheader sale
                CROSS JOIN (SELECT CAST(? AS uuid) AS event_id) evt
                WHERE sale.sale_id = ?
                """, eventId, saleId);

        return saleId;
    }
}
