package com.sameer.sdl.system_design_labs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class KioskInventoryService {
    private static final Logger log =
            LoggerFactory.getLogger(KioskInventoryService.class);

    private final JdbcTemplate jdbc;

    public KioskInventoryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public boolean applySale(UUID eventId, List<String> itemSkus) {
        log.info("applySale: Checking eventId={} itemSkus={}", eventId, itemSkus);

        int inserted = jdbc.update("""
                INSERT INTO kskdb.kiosk_inventory_processed_events (event_id)
                VALUES (?)
                ON CONFLICT (event_id) DO NOTHING
                """, eventId);

        if (inserted == 0) {
            log.info("applySale: Duplicate eventId={} itemSkus={}; skipping stock deduction",
                    eventId, itemSkus);
            return false;
        }

        // Combine repeated SKUs and update stock in a consistent order.
        var quantities = new TreeMap<String, Integer>();
        for (var sku : itemSkus) {
            quantities.merge(sku, 1, Integer::sum);
        }

        for (var item : quantities.entrySet()) {
            int updated = jdbc.update("""
                    UPDATE kskdb.kiosk_inventory
                    SET quantity = quantity - ?
                    WHERE item_sku = ? AND quantity >= ?
                    """, item.getValue(), item.getKey(), item.getValue());

            if (updated == 0) {
                // The exception rolls back every deduction and the processed-event record.
                throw new IllegalStateException("Insufficient stock for SKU " + item.getKey());
            }
        }
        return true;
    }
}
