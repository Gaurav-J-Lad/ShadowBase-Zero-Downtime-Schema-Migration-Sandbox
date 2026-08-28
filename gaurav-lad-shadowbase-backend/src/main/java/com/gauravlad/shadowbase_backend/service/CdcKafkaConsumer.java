package com.gauravlad.shadowbase_backend.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CdcKafkaConsumer {

    private final CdcEventProcessor cdcEventProcessor;

    public CdcKafkaConsumer(CdcEventProcessor cdcEventProcessor) {
        this.cdcEventProcessor = cdcEventProcessor;
    }

    @KafkaListener(
            topics = "shadowbase.public.customers",
            groupId = "shadowbase-cdc-consumer"
    )
    public void consume(String message) {

        System.out.println("======================================");
        System.out.println("CDC MESSAGE RECEIVED FROM KAFKA");
        System.out.println("======================================");

        System.out.println(message);

        cdcEventProcessor.process(message);
    }
}