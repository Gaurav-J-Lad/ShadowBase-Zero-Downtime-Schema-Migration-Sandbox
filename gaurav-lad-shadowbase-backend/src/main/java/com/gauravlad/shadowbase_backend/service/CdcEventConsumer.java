package com.gauravlad.shadowbase_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CdcEventConsumer {

    private final ObjectMapper objectMapper;
    private final CdcEventApplier cdcEventApplier;
    private final CdcEnvironmentResolver environmentResolver;

    public CdcEventConsumer(
            ObjectMapper objectMapper,
            CdcEventApplier cdcEventApplier,
            CdcEnvironmentResolver environmentResolver) {

        this.objectMapper = objectMapper;
        this.cdcEventApplier = cdcEventApplier;
        this.environmentResolver = environmentResolver;
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

            if (operationNode == null ||
                    operationNode.isNull()) {

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

            /*
             * Find the target shadow environment
             * dynamically.
             */
            Long environmentId =
                    environmentResolver
                            .resolveTargetEnvironment();

            System.out.println(
                    "Target Environment: "
                            + environmentId
            );

            switch (operation) {

                case "c" ->
                        handleInsert(
                                environmentId,
                                payload
                        );

                case "u" ->
                        handleUpdate(
                                environmentId,
                                payload
                        );

                case "d" ->
                        handleDelete(
                                environmentId,
                                payload
                        );

                case "r" ->
                        handleSnapshot(
                                environmentId,
                                payload
                        );

                default ->
                        System.out.println(
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

    /*
     * INSERT
     */
    private void handleInsert(
            Long environmentId,
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
                        + environmentId
        );

        cdcEventApplier.applyInsert(
                environmentId,
                after
        );
    }

    /*
     * UPDATE
     */
    private void handleUpdate(
            Long environmentId,
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
                        + environmentId
        );

        cdcEventApplier.applyUpdate(
                environmentId,
                after
        );
    }

    /*
     * DELETE
     */
    private void handleDelete(
            Long environmentId,
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
                        + environmentId
        );

        cdcEventApplier.applyDelete(
                environmentId,
                before
        );
    }

    /*
     * SNAPSHOT
     */
    private void handleSnapshot(
            Long environmentId,
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
                        + environmentId
        );

        cdcEventApplier.applyInsert(
                environmentId,
                after
        );
    }
}