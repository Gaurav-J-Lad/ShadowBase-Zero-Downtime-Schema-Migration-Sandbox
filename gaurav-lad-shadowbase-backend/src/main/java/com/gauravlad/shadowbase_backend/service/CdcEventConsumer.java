package com.gauravlad.shadowbase_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class CdcEventConsumer {

    private final ObjectMapper objectMapper;
    private final CdcEventApplier cdcEventApplier;
    private final DataSource shadowDataSource;

    public CdcEventConsumer(
            ObjectMapper objectMapper,
            CdcEventApplier cdcEventApplier,
            DataSource shadowDataSource
    ) {
        this.objectMapper = objectMapper;
        this.cdcEventApplier = cdcEventApplier;
        this.shadowDataSource = shadowDataSource;
    }

    @KafkaListener(
            topics = "shadowbase.public.customers",
            groupId = "shadowbase-cdc-consumer"
    )
    public void consume(String message) {

        try {
            System.out.println("CDC EVENT RECEIVED:");
            System.out.println(message);

            JsonNode root = objectMapper.readTree(message);

            JsonNode payload = root.get("payload");

            if (payload == null || payload.isNull()) {
                System.out.println("No payload found.");
                return;
            }

            String operation = payload
                    .get("op")
                    .asText();

            System.out.println("CDC Operation: " + operation);

            switch (operation) {

                case "c":
                    handleInsert(payload);
                    break;

                case "u":
                    System.out.println("UPDATE event received.");
                    break;

                case "d":
                    System.out.println("DELETE event received.");
                    break;

                case "r":
                    System.out.println("READ/SNAPSHOT event received.");
                    break;

                default:
                    System.out.println(
                            "Unknown CDC operation: " + operation
                    );
            }

        } catch (Exception e) {
            System.err.println(
                    "Failed to process CDC event: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    private void handleInsert(JsonNode payload) {

        JsonNode after = payload.get("after");

        if (after == null || after.isNull()) {
            System.out.println("INSERT event has no 'after' data.");
            return;
        }

        String name = after
                .get("name")
                .asText();

        String email = after
                .get("email")
                .asText();

        System.out.println(
                "Applying INSERT: "
                        + name + " / " + email
        );

        cdcEventApplier.applyInsert(
                shadowDataSource,
                "customers",
                name,
                email
        );
    }
}