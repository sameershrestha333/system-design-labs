package com.sameer.sdl.system_design_labs.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.sameer.sdl.system_design_labs.service.KioskInventoryService;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Component
public class SaleEventConsumers {

    private final KioskInventoryService inventory;
    private final ObjectMapper objectMapper;

    public SaleEventConsumers(KioskInventoryService inventory, ObjectMapper objectMapper) {
        this.inventory = inventory;
        this.objectMapper = objectMapper;
    }

    private static final Logger log = LoggerFactory.getLogger(SaleEventConsumers.class);

    @KafkaListener(id = "adm-listener", groupId = "adm-reporting",
            topics = SalesKafkaConfig.SALE_EVENTS)
    public void updateAdmReport(ConsumerRecord<String, String> event) {
        logSale("updateAdmReport", event);
    }

    @KafkaListener(id = "inventory-listener", groupId = "inventory",
            topics = SalesKafkaConfig.SALE_EVENTS)
    public void updateInventory(ConsumerRecord<String, String> event) {
        var payload = objectMapper.readTree(event.value());
        var eventId = UUID.fromString(payload.get("eventId").asText());
        var itemSku = payload.get("itemSku").asText();

        log.info("updateInventory: Received eventId={} saleId={} itemSku={} partition={} offset={}",
                eventId, event.key(), itemSku, event.partition(), event.offset());

        boolean applied = inventory.applySale(eventId, itemSku);

        if (applied) {
            log.info("updateInventory: STOCK_DEDUCTED eventId={} itemSku={} deducted=1; transaction committed",
                    eventId, itemSku);
        } else {
            log.info("updateInventory: DUPLICATE_SKIPPED eventId={} itemSku={} offset={}",
                    eventId, itemSku, event.offset());
        }
    }

    @KafkaListener(id = "receipt-listener", groupId = "receipts",
            topics = SalesKafkaConfig.SALE_EVENTS)
    public void deliverReceipt(ConsumerRecord<String, String> event) {
        logSale("deliverReceipt", event);
    }

    private void logSale(String method, ConsumerRecord<String, String> event) {
        log.info("{}: saleId={} partition={} offset={} payload={}",
                method, event.key(), event.partition(), event.offset(), event.value());
    }

}
