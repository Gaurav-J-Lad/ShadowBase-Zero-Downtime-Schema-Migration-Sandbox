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
            topicPattern = "shadowbase\\.public\\..*",
            groupId = "shadowbase-cdc-consumer"
    )
    public void consume(String message) {

        System.out.println("========================================");
        System.out.println("Received CDC event:");
        System.out.println(message);

        try {

            CdcEvent event =
                    objectMapper.readValue(
                            message,
                            CdcEvent.class
                    );

            if (event.source() == null) {
                throw new RuntimeException(
                        "CDC event has no source information"
                );
            }

            String sourceDatabase =
                    event.source().database();

            String sourceSchema =
                    event.source().schema();

            Long environmentId =
                    environmentResolver.resolveTargetEnvironment(
                            sourceDatabase,
                            sourceSchema
                    );

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

            System.out.println("========================================");

        } catch (Exception e) {

            System.err.println(
                    "CDC event processing failed"
            );

            e.printStackTrace();

            /*
             * IMPORTANT:
             * Re-throw the exception so Spring Kafka's
             * error handler can retry the message.
             */
            throw new RuntimeException(
                    "Failed to process CDC event",
                    e
            );
        }
    }
}