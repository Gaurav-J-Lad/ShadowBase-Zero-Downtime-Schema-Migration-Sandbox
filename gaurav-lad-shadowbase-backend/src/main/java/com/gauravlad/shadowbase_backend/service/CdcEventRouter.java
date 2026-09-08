package com.gauravlad.shadowbase_backend.service;

import com.gauravlad.shadowbase_backend.dto.CdcEvent;
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

        this.customerCdcApplier = customerCdcApplier;
        this.productCdcApplier = productCdcApplier;
        this.orderCdcApplier = orderCdcApplier;
    }

    public void route(
            Long environmentId,
            CdcEvent event) {

        if (event.source() == null) {
            throw new RuntimeException(
                    "CDC event has no source information"
            );
        }

        String tableName = event.source().table();

        if (tableName == null || tableName.isBlank()) {
            throw new RuntimeException(
                    "CDC event has no table information"
            );
        }

        System.out.println(
                "CDC Table: " + tableName
        );

        switch (tableName) {

            case "customers" ->
                    routeCustomers(
                            environmentId,
                            event
                    );

            case "products" ->
                    routeProducts(
                            environmentId,
                            event
                    );

            case "orders" ->
                    routeOrders(
                            environmentId,
                            event
                    );

            default ->
                    System.out.println(
                            "No CDC handler configured for table: "
                                    + tableName
                    );
        }
    }

    private void routeCustomers(
            Long environmentId,
            CdcEvent event) {

        switch (event.operation()) {

            case "c" -> {
                if (event.after() == null) return;

                customerCdcApplier.applyInsert(
                        environmentId,
                        event.after()
                );
            }

            case "u" -> {
                if (event.after() == null) return;

                customerCdcApplier.applyUpdate(
                        environmentId,
                        event.after()
                );
            }

            case "d" -> {
                if (event.before() == null) return;

                customerCdcApplier.applyDelete(
                        environmentId,
                        event.before()
                );
            }

            case "r" -> {
                if (event.after() == null) return;

                customerCdcApplier.applyInsert(
                        environmentId,
                        event.after()
                );
            }

            default ->
                    logUnknownOperation(
                            event.operation(),
                            "customers"
                    );
        }
    }

    private void routeProducts(
            Long environmentId,
            CdcEvent event) {

        switch (event.operation()) {

            case "c" -> {
                if (event.after() == null) return;

                productCdcApplier.applyInsert(
                        environmentId,
                        event.after()
                );
            }

            case "u" -> {
                if (event.after() == null) return;

                productCdcApplier.applyUpdate(
                        environmentId,
                        event.after()
                );
            }

            case "d" -> {
                if (event.before() == null) return;

                productCdcApplier.applyDelete(
                        environmentId,
                        event.before()
                );
            }

            case "r" -> {
                if (event.after() == null) return;

                productCdcApplier.applyInsert(
                        environmentId,
                        event.after()
                );
            }

            default ->
                    logUnknownOperation(
                            event.operation(),
                            "products"
                    );
        }
    }

    private void routeOrders(
            Long environmentId,
            CdcEvent event) {

        switch (event.operation()) {

            case "c" -> {
                if (event.after() == null) return;

                orderCdcApplier.applyInsert(
                        environmentId,
                        event.after()
                );
            }

            case "u" -> {
                if (event.after() == null) return;

                orderCdcApplier.applyUpdate(
                        environmentId,
                        event.after()
                );
            }

            case "d" -> {
                if (event.before() == null) return;

                orderCdcApplier.applyDelete(
                        environmentId,
                        event.before()
                );
            }

            case "r" -> {
                if (event.after() == null) return;

                orderCdcApplier.applyInsert(
                        environmentId,
                        event.after()
                );
            }

            default ->
                    logUnknownOperation(
                            event.operation(),
                            "orders"
                    );
        }
    }

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