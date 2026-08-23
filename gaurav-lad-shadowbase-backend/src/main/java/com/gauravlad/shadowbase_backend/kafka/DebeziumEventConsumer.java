package com.gauravlad.shadowbase_backend.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DebeziumEventConsumer {

    private final ObjectMapper objectMapper;

    public DebeziumEventConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "shadowbase.public.customers",
            groupId = "shadowbase-replayer"
    )
    public void consume(String message) {

        try {

            JsonNode root =
                    objectMapper.readTree(message);

            JsonNode payload =
                    root.get("payload");

            String operation =
                    payload.get("op").asText();

            JsonNode before =
                    payload.get("before");

            JsonNode after =
                    payload.get("after");

            System.out.println(
                    "===================================="
            );

            System.out.println(
                    "CDC EVENT RECEIVED"
            );

            System.out.println(
                    "Operation: " + operation
            );

            System.out.println(
                    "Before: " + before
            );

            System.out.println(
                    "After: " + after
            );

            System.out.println(
                    "===================================="
            );

        } catch (Exception e) {

            System.err.println(
                    "Failed to process CDC event: "
                            + e.getMessage()
            );
        }
    }
}