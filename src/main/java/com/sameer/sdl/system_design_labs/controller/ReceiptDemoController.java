package com.sameer.sdl.system_design_labs.controller;

import org.springframework.http.HttpStatus;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Objects;


@RestController
@RequestMapping("/kafka-demo/receipts")
public class ReceiptDemoController {
    private final KafkaListenerEndpointRegistry registry;

    public ReceiptDemoController(KafkaListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    @PostMapping("/stop")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void stop() {
        receiptListener().stop();
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void start() {
        receiptListener().start();
    }

    private MessageListenerContainer receiptListener() {
        return Objects.requireNonNull(registry.getListenerContainer("receipt-listener"));
    }
}
