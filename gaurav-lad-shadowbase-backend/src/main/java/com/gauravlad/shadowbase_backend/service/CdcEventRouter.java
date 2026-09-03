package com.gauravlad.shadowbase_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class CdcEventRouter {

    private final CdcEventApplier cdcEventApplier;

    public CdcEventRouter(
            CdcEventApplier cdcEventApplier) {

        this.cdcEventApplier =
                cdcEventApplier;
    }

    /*
     * Route a CDC event to the correct table handler.
     */
    public void route(
            Long environmentId,
            JsonNode payload) {

        JsonNode source =
                payload.get("source");

        if (source == null || source.isNull()) {

            throw new RuntimeException(
                    "CDC event has no source information"
            );
        }

        JsonNode tableNode =
                source.get("table");

        if (tableNode == null ||
                tableNode.isNull()) {

            throw new RuntimeException(
                    "CDC event has no table information"
            );
        }

        String tableName =
                tableNode.asText();

        System.out.println(
                "CDC Table: " + tableName
        );

        switch (tableName) {

            case "customers" ->
                    routeCustomers(
                            environmentId,
                            payload
                    );

            default ->
                    System.out.println(
                            "No CDC handler configured for table: "
                                    + tableName
                    );
        }
    }

    /*
     * Route customers table events.
     */
    private void routeCustomers(
            Long environmentId,
            JsonNode payload) {

        JsonNode operationNode =
                payload.get("op");

        if (operationNode == null ||
                operationNode.isNull()) {

            throw new RuntimeException(
                    "CDC event has no operation"
            );
        }

        String operation =
                operationNode.asText();

        switch (operation) {

            case "c" -> {

                JsonNode after =
                        payload.get("after");

                if (after != null &&
                        !after.isNull()) {

                    cdcEventApplier.applyInsert(
                            environmentId,
                            after
                    );
                }
            }

            case "u" -> {

                JsonNode after =
                        payload.get("after");

                if (after != null &&
                        !after.isNull()) {

                    cdcEventApplier.applyUpdate(
                            environmentId,
                            after
                    );
                }
            }

            case "d" -> {

                JsonNode before =
                        payload.get("before");

                if (before != null &&
                        !before.isNull()) {

                    cdcEventApplier.applyDelete(
                            environmentId,
                            before
                    );
                }
            }

            case "r" -> {

                JsonNode after =
                        payload.get("after");

                if (after != null &&
                        !after.isNull()) {

                    cdcEventApplier.applyInsert(
                            environmentId,
                            after
                    );
                }
            }

            default ->
                    System.out.println(
                            "Unknown CDC operation: "
                                    + operation
                    );
        }
    }
}
