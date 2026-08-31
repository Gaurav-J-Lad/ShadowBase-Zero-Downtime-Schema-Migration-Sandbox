package com.gauravlad.shadowbase_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CdcEventConsumer {

    private final ObjectMapper objectMapper;
    private final CdcEventApplier cdcEventApplier;

    /*
     * Temporary environment ID.
     *
     * We will make this dynamic later.
     * Your current working shadow environment is 28.
     */
    private static final Long ENVIRONMENT_ID = 28L;

    public CdcEventConsumer(
            ObjectMapper objectMapper,
            CdcEventApplier cdcEventApplier) {

        this.objectMapper = objectMapper;
        this.cdcEventApplier = cdcEventApplier;
    }

    @KafkaListener(
            topics = "shadowbase.public.customers",
            groupId = "shadowbase-cdc-consumer"
    )
    public void consume(String message) {

        try {

            System.out.println();
            System.out.println("======================================");
            System.out.println("CDC EVENT RECEIVED");
            System.out.println("======================================");
            System.out.println(message);

            JsonNode root =
                    objectMapper.readTree(message);

            JsonNode payload =
                    root.get("payload");

            if (payload == null || payload.isNull()) {

                System.out.println(
                        "CDC event has no payload."
                );

                return;
            }

            JsonNode operationNode =
                    payload.get("op");

            if (operationNode == null
                    || operationNode.isNull()) {

                System.out.println(
                        "CDC event has no operation."
                );

                return;
            }

            String operation =
                    operationNode.asText();

            System.out.println(
                    "CDC Operation: " + operation
            );

            switch (operation) {

                // INSERT
                case "c" -> handleInsert(payload);

                // UPDATE
                case "u" -> handleUpdate(payload);

                // DELETE
                case "d" -> handleDelete(payload);

                // SNAPSHOT
                case "r" -> handleSnapshot(payload);

                default -> System.out.println(
                        "Unknown CDC operation: "
                                + operation
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Error processing CDC event: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    private void handleInsert(
            JsonNode payload) {

        JsonNode after =
                payload.get("after");

        if (after == null || after.isNull()) {

            System.out.println(
                    "INSERT event has no 'after' data."
            );

            return;
        }

        System.out.println(
                "Applying INSERT to environment "
                        + ENVIRONMENT_ID
        );

        cdcEventApplier.applyInsert(
                ENVIRONMENT_ID,
                after
        );
    }

    private void handleUpdate(
            JsonNode payload) {

        JsonNode after =
                payload.get("after");

        if (after == null || after.isNull()) {

            System.out.println(
                    "UPDATE event has no 'after' data."
            );

            return;
        }

        System.out.println(
                "Applying UPDATE to environment "
                        + ENVIRONMENT_ID
        );

        cdcEventApplier.applyUpdate(
                ENVIRONMENT_ID,
                after
        );
    }

    private void handleDelete(
            JsonNode payload) {

        JsonNode before =
                payload.get("before");

        if (before == null || before.isNull()) {

            System.out.println(
                    "DELETE event has no 'before' data."
            );

            return;
        }

        System.out.println(
                "Applying DELETE to environment "
                        + ENVIRONMENT_ID
        );

        cdcEventApplier.applyDelete(
                ENVIRONMENT_ID,
                before
        );
    }

    private void handleSnapshot(
            JsonNode payload) {

        JsonNode after =
                payload.get("after");

        if (after == null || after.isNull()) {

            System.out.println(
                    "SNAPSHOT event has no 'after' data."
            );

            return;
        }

        System.out.println(
                "Applying SNAPSHOT to environment "
                        + ENVIRONMENT_ID
        );

        cdcEventApplier.applyInsert(
                ENVIRONMENT_ID,
                after
        );
    }
}