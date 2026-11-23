package com.sameer.sdl.system_design_labs.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SalesKafkaConfig {

    public static final String SALE_EVENTS = "SaleEvents";

    @Bean
    NewTopic saleEventsTopic() {
        return new NewTopic(SALE_EVENTS, 2, (short) 1);
    }
}
