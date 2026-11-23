package com.sameer.sdl.system_design_labs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public boolean applySale(UUID eventId, String itemSku) {
        log.info("applySale: Checking eventId={} itemSku={}", eventId, itemSku);

        int inserted = jdbc.update("""
                INSERT INTO kiosk_inventory_processed_events (event_id)
                VALUES (?)
                ON CONFLICT (event_id) DO NOTHING
                """, eventId);

        if (inserted == 0) {
            log.info("applySale: Duplicate9++++ eventId={} itemSku={}; skipping stock deduction",
                    eventId, itemSku);
            return false;
        }

        int updated = jdbc.update("""
                UPDATE kiosk_inventory
                SET quantity = quantity - 1
                WHERE item_sku = ? AND quantity > 0
                """, itemSku);

        if (updated == 0) {
            log.warn("applySale: No available stock for itemSku={} eventId={}; rolling back",
                    itemSku, eventId);
            throw new IllegalStateException("No available stock for SKU " + itemSku);
        }

        log.info("applySale: Stock update succeeded for eventId={} itemSku={} deducted=1; awaiting commit",
                eventId, itemSku);
        return true;
    }
}
