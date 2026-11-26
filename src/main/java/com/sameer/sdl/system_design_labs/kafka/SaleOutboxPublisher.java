package com.sameer.sdl.system_design_labs.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class SaleOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(SaleOutboxPublisher.class);
    private final JdbcTemplate jdbc;
    private final KafkaTemplate<String, String> kafka;

    public SaleOutboxPublisher(JdbcTemplate jdbc, KafkaTemplate<String, String> kafka) {
        this.jdbc = jdbc;
        this.kafka = kafka;
    }

    @Scheduled(fixedDelay = 1000)
    public void publishPendingSales() throws Exception {
        var events = jdbc.query("""
                SELECT event_id, sale_id, payload::text AS payload
                FROM kskdb.kiosk_sale_outbox
                WHERE published_at IS NULL
                ORDER BY created_at, event_id
                LIMIT 10
                """, (rs, rowNum) -> new OutboxEvent(
                rs.getObject("event_id", UUID.class),
                rs.getObject("sale_id", UUID.class),
                rs.getString("payload")));

        for (var event : events) {
            var result = kafka.send(
                    SalesKafkaConfig.SALE_EVENTS,
                    event.saleId().toString(),
                    event.payload()
            ).get(10, TimeUnit.SECONDS);

            jdbc.update("""
                    UPDATE kskdb.kiosk_sale_outbox
                    SET published_at = CURRENT_TIMESTAMP
                    WHERE event_id = ?
                    """, event.eventId());

            var metadata = result.getRecordMetadata();
            log.info("publishPendingSales: eventId={} saleId={} partition={} offset={}",
                    event.eventId(), event.saleId(), metadata.partition(), metadata.offset());
        }
    }

    private record OutboxEvent(UUID eventId, UUID saleId, String payload) {}

}
