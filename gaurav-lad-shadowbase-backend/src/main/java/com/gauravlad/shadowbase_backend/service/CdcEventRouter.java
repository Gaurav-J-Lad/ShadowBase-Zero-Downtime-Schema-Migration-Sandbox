package com.gauravlad.shadowbase_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class CdcEventRouter {

    private final CustomerCdcApplier customerCdcApplier;
    private final ProductCdcApplier productCdcApplier;
    private final OrderCdcApplier orderCdcApplier;

    public CdcEventRouter(
            CustomerCdcApplier customerCdcApplier,
            ProductCdcApplier productCdcApplier,
            OrderCdcApplier orderCdcApplier) {

        this.customerCdcApplier =
                customerCdcApplier;

        this.productCdcApplier =
                productCdcApplier;

        this.orderCdcApplier =
                orderCdcApplier;
    }

    /*
     * Route CDC event to the correct table handler.
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

            case "products" ->
                    routeProducts(
                            environmentId,
                            payload
                    );

            case "orders" ->
                    routeOrders(
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

        String operation =
                getOperation(payload);

        switch (operation) {

            case "c" -> {

                JsonNode after =
                        getAfter(payload);

                if (after == null) {
                    return;
                }

                customerCdcApplier.applyInsert(
                        environmentId,
                        after
                );
            }

            case "u" -> {

                JsonNode after =
                        getAfter(payload);

                if (after == null) {
                    return;
                }

                customerCdcApplier.applyUpdate(
                        environmentId,
                        after
                );
            }

            case "d" -> {

                JsonNode before =
                        getBefore(payload);

                if (before == null) {
                    return;
                }

                customerCdcApplier.applyDelete(
                        environmentId,
                        before
                );
            }

            case "r" -> {

                JsonNode after =
                        getAfter(payload);

                if (after == null) {
                    return;
                }

                customerCdcApplier.applyInsert(
                        environmentId,
                        after
                );
            }

            default ->
                    logUnknownOperation(
                            operation,
                            "customers"
                    );
        }
    }

    /*
     * Route products table events.
     */
    private void routeProducts(
            Long environmentId,
            JsonNode payload) {

        String operation =
                getOperation(payload);

        switch (operation) {

            case "c" -> {

                JsonNode after =
                        getAfter(payload);

                if (after == null) {
                    return;
                }

                productCdcApplier.applyInsert(
                        environmentId,
                        after
                );
            }

            case "u" -> {

                JsonNode after =
                        getAfter(payload);

                if (after == null) {
                    return;
                }

                productCdcApplier.applyUpdate(
                        environmentId,
                        after
                );
            }

            case "d" -> {

                JsonNode before =
                        getBefore(payload);

                if (before == null) {
                    return;
                }

                productCdcApplier.applyDelete(
                        environmentId,
                        before
                );
            }

            case "r" -> {

                JsonNode after =
                        getAfter(payload);

                if (after == null) {
                    return;
                }

                productCdcApplier.applyInsert(
                        environmentId,
                        after
                );
            }

            default ->
                    logUnknownOperation(
                            operation,
                            "products"
                    );
        }
    }

    /*
     * Route orders table events.
     */
    private void routeOrders(
            Long environmentId,
            JsonNode payload) {

        String operation =
                getOperation(payload);

        switch (operation) {

            case "c" -> {

                JsonNode after =
                        getAfter(payload);

                if (after == null) {
                    return;
                }

                orderCdcApplier.applyInsert(
                        environmentId,
                        after
                );
            }

            case "u" -> {

                JsonNode after =
                        getAfter(payload);

                if (after == null) {
                    return;
                }

                orderCdcApplier.applyUpdate(
                        environmentId,
                        after
                );
            }

            case "d" -> {

                JsonNode before =
                        getBefore(payload);

                if (before == null) {
                    return;
                }

                orderCdcApplier.applyDelete(
                        environmentId,
                        before
                );
            }

            case "r" -> {

                JsonNode after =
                        getAfter(payload);

                if (after == null) {
                    return;
                }

                orderCdcApplier.applyInsert(
                        environmentId,
                        after
                );
            }

            default ->
                    logUnknownOperation(
                            operation,
                            "orders"
                    );
        }
    }

    /*
     * Get CDC operation.
     */
    private String getOperation(
            JsonNode payload) {

        JsonNode operationNode =
                payload.get("op");

        if (operationNode == null ||
                operationNode.isNull()) {

            throw new RuntimeException(
                    "CDC event has no operation"
            );
        }

        return operationNode.asText();
    }

    /*
     * Get 'after' data.
     */
    private JsonNode getAfter(
            JsonNode payload) {

        JsonNode after =
                payload.get("after");

        if (after == null ||
                after.isNull()) {

            System.out.println(
                    "CDC event has no 'after' data."
            );

            return null;
        }

        return after;
    }

    /*
     * Get 'before' data.
     */
    private JsonNode getBefore(
            JsonNode payload) {

        JsonNode before =
                payload.get("before");

        if (before == null ||
                before.isNull()) {

            System.out.println(
                    "CDC event has no 'before' data."
            );

            return null;
        }

        return before;
    }

    /*
     * Log unknown operation.
     */
    private void logUnknownOperation(
            String operation,
            String tableName) {

        System.out.println(
                "Unknown CDC operation '"
                        + operation
                        + "' for table '"
                        + tableName
                        + "'"
        );
    }
}
