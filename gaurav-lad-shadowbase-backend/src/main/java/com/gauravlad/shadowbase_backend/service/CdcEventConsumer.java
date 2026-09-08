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
            topics = {
                    "shadowbase.public.customers",
                    "shadowbase.public.products",
                    "shadowbase.public.orders"
            },
            groupId = "shadowbase-cdc-consumer"
    )
    public void consume(String message) {

        try {

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "Received CDC event:"
            );

            System.out.println(message);

            CdcEvent event =
                    objectMapper.readValue(
                            message,
                            CdcEvent.class
                    );

            Long environmentId =
                    environmentResolver
                            .resolveTargetEnvironment();

            System.out.println(
                    "Resolved target environment: "
                            + environmentId
            );

            cdcEventRouter.route(
                    environmentId,
                    event
            );

            System.out.println(
                    "CDC event successfully processed"
            );

            System.out.println(
                    "========================================"
            );

        } catch (Exception e) {

            System.err.println(
                    "Failed to process CDC event"
            );

            e.printStackTrace();
        }
    }
}