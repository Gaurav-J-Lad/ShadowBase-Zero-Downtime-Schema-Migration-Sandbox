package com.gauravlad.shadowbase_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gauravlad.shadowbase_backend.dto.CdcEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CdcEventConsumer {

    private final ObjectMapper objectMapper;
    private final CdcEnvironmentResolver environmentResolver;
    private final CdcEventRouter cdcEventRouter;

    public CdcEventConsumer(
            ObjectMapper objectMapper,
            CdcEnvironmentResolver environmentResolver,
            CdcEventRouter cdcEventRouter) {

        this.objectMapper = objectMapper;
        this.environmentResolver = environmentResolver;
        this.cdcEventRouter = cdcEventRouter;
    }

    @KafkaListener(
            topics = "shadowbase.public.customers",
            groupId = "shadowbase-cdc-consumer"
    )
    public void consume(String message) {

        try {

            System.out.println(
                    "Received CDC message: " + message
            );

            // Convert Kafka JSON into CdcEvent
            CdcEvent event =
                    objectMapper.readValue(
                            message,
                            CdcEvent.class
                    );

            // Find the currently running shadow environment
            Long environmentId =
                    environmentResolver
                            .resolveTargetEnvironment();

            // Send event to router
            cdcEventRouter.route(
                    environmentId,
                    event
            );

        } catch (Exception e) {

            System.err.println(
                    "Failed to process CDC event"
            );

            e.printStackTrace();
        }
    }
}