package com.gauravlad.shadowbase_backend.service;

import com.gauravlad.shadowbase_backend.dto.CdcEvent;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CdcEventRouter {

    private final Map<String, CdcTableApplier> appliers;

    public CdcEventRouter(
            List<CdcTableApplier> appliers) {

        this.appliers =
                appliers.stream()
                        .collect(
                                Collectors.toMap(
                                        CdcTableApplier::getTableName,
                                        Function.identity()
                                )
                        );

        System.out.println(
                "Registered CDC table appliers: "
                        + this.appliers.keySet()
        );
    }

    public void route(
            Long environmentId,
            CdcEvent event) {

        if (event.source() == null) {

            throw new RuntimeException(
                    "CDC event has no source information"
            );
        }

        String tableName =
                event.source().table();

        if (tableName == null
                || tableName.isBlank()) {

            throw new RuntimeException(
                    "CDC event has no table information"
            );
        }

        String operation =
                event.operation();

        if (operation == null
                || operation.isBlank()) {

            throw new RuntimeException(
                    "CDC event has no operation"
            );
        }

        CdcTableApplier applier =
                appliers.get(tableName);

        if (applier == null) {

            System.out.println(
                    "No CDC handler configured for table: "
                            + tableName
            );

            return;
        }

        System.out.println(
                "CDC Table: " + tableName
        );

        System.out.println(
                "CDC Operation: " + operation
        );

        switch (operation) {

            case "c" -> {

                JsonNode after =
                        event.after();

                if (after == null) {
                    return;
                }

                applier.applyInsert(
                        environmentId,
                        after
                );
            }

            case "u" -> {

                JsonNode after =
                        event.after();

                if (after == null) {
                    return;
                }

                applier.applyUpdate(
                        environmentId,
                        after
                );
            }

            case "d" -> {

                JsonNode before =
                        event.before();

                if (before == null) {
                    return;
                }

                applier.applyDelete(
                        environmentId,
                        before
                );
            }

            case "r" -> {

                JsonNode after =
                        event.after();

                if (after == null) {
                    return;
                }

                applier.applyInsert(
                        environmentId,
                        after
                );
            }

            default ->
                    System.out.println(
                            "Unknown CDC operation '"
                                    + operation
                                    + "' for table '"
                                    + tableName
                                    + "'"
                    );
        }
    }
}