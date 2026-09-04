package com.gauravlad.shadowbase_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class CdcEventRouter {

    private final CustomerCdcApplier customerCdcApplier;

    public CdcEventRouter(
            CustomerCdcApplier customerCdcApplier) {

        this.customerCdcApplier =
                customerCdcApplier;
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

            /*
             * INSERT
             */
            case "c" -> {

                JsonNode after =
                        payload.get("after");

                if (after == null ||
                        after.isNull()) {

                    System.out.println(
                            "INSERT event has no 'after' data."
                    );

                    return;
                }

                customerCdcApplier.applyInsert(
                        environmentId,
                        after
                );
            }

            /*
             * UPDATE
             */
            case "u" -> {

                JsonNode after =
                        payload.get("after");

                if (after == null ||
                        after.isNull()) {

                    System.out.println(
                            "UPDATE event has no 'after' data."
                    );

                    return;
                }

                customerCdcApplier.applyUpdate(
                        environmentId,
                        after
                );
            }

            /*
             * DELETE
             */
            case "d" -> {

                JsonNode before =
                        payload.get("before");

                if (before == null ||
                        before.isNull()) {

                    System.out.println(
                            "DELETE event has no 'before' data."
                    );

                    return;
                }

                customerCdcApplier.applyDelete(
                        environmentId,
                        before
                );
            }

            /*
             * SNAPSHOT
             */
            case "r" -> {

                JsonNode after =
                        payload.get("after");

                if (after == null ||
                        after.isNull()) {

                    System.out.println(
                            "SNAPSHOT event has no 'after' data."
                    );

                    return;
                }

                customerCdcApplier.applyInsert(
                        environmentId,
                        after
                );
            }

            /*
             * Unknown operation
             */
            default ->
                    System.out.println(
                            "Unknown CDC operation: "
                                    + operation
                    );
        }
    }
}
