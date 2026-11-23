package com.sameer.sdl.system_design_labs.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SaleEventConsumers {

    private static final Logger log = LoggerFactory.getLogger(SaleEventConsumers.class);

    @KafkaListener(id = "adm-listener", groupId = "adm-reporting",
            topics = SalesKafkaConfig.SALE_EVENTS)
    public void updateAdmReport(ConsumerRecord<String, String> event) {
        logSale("updateAdmReport", event);
    }

    @KafkaListener(id = "inventory-listener", groupId = "inventory",
            topics = SalesKafkaConfig.SALE_EVENTS)
    public void updateInventory(ConsumerRecord<String, String> event) {
        logSale("updateInventory", event);
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
