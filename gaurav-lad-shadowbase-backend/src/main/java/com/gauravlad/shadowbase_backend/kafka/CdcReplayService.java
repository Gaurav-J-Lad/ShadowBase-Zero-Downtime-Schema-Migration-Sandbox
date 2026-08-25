package com.gauravlad.shadowbase_backend.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.gauravlad.shadowbase_backend.environment.ShadowDatabaseManager;
import org.springframework.stereotype.Service;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

@Service
public class CdcReplayService {

    private final ShadowDatabaseManager shadowDatabaseManager;

    /*
     * Temporary environment.
     *
     * Later this will become dynamic.
     */
    private static final Long SHADOW_ENVIRONMENT_ID = 17L;

    /*
     * PostgreSQL version used by the shadow database.
     */
    private static final String POSTGRES_VERSION = "16";

    public CdcReplayService(
            ShadowDatabaseManager shadowDatabaseManager) {

        this.shadowDatabaseManager = shadowDatabaseManager;
    }

    /**
     * Replay one Debezium CDC event against
     * the shadow PostgreSQL database.
     */
    public void replay(JsonNode event) {

        long startTime = System.currentTimeMillis();

        try {

            /*
             * ================================
             * PAYLOAD
             * ================================
             */

            JsonNode payload =
                    event.get("payload");

            if (payload == null || payload.isNull()) {

                System.out.println(
                        "CDC payload is null"
                );

                return;
            }

            /*
             * ================================
             * OPERATION
             * ================================
             */

            JsonNode operationNode =
                    payload.get("op");

            if (operationNode == null
                    || operationNode.isNull()) {

                System.out.println(
                        "CDC operation is missing"
                );

                return;
            }

            String operation =
                    operationNode.asText();

            /*
             * ================================
             * SOURCE
             * ================================
             */

            JsonNode source =
                    payload.get("source");

            if (source == null
                    || source.isNull()) {

                System.out.println(
                        "CDC source is missing"
                );

                return;
            }

            /*
             * ================================
             * TABLE
             * ================================
             */

            JsonNode tableNode =
                    source.get("table");

            if (tableNode == null
                    || tableNode.isNull()) {

                System.out.println(
                        "CDC table is missing"
                );

                return;
            }

            String table =
                    tableNode.asText();

            /*
             * ================================
             * LOG EVENT
             * ================================
             */

            System.out.println();

            System.out.println(
                    "======================================"
            );

            System.out.println(
                    "CDC EVENT"
            );

            System.out.println(
                    "Operation  : " + operation
            );

            System.out.println(
                    "Table      : " + table
            );

            System.out.println(
                    "Environment: "
                            + SHADOW_ENVIRONMENT_ID
            );

            System.out.println(
                    "======================================"
            );

            /*
             * ================================
             * TABLE SUPPORT
             * ================================
             */

            if (!"customers".equalsIgnoreCase(table)) {

                System.out.println(
                        "Ignoring unsupported table: "
                                + table
                );

                return;
            }

            /*
             * ================================
             * GET OR CREATE SHADOW CONTAINER
             * ================================
             *
             * This is important.
             *
             * If Spring Boot restarted, the
             * in-memory container map is empty.
             *
             * getOrCreateContainer() will create
             * a new Testcontainer automatically.
             */

            PostgreSQLContainer<?> container =
                    shadowDatabaseManager
                            .getOrCreateContainer(
                                    SHADOW_ENVIRONMENT_ID,
                                    POSTGRES_VERSION
                            );

            /*
             * ================================
             * DATABASE CONNECTION
             * ================================
             */

            try (
                    Connection connection =
                            container.createConnection("")
            ) {

                switch (operation) {

                    /*
                     * INSERT
                     */
                    case "c" -> handleInsert(
                            connection,
                            payload.get("after")
                    );

                    /*
                     * UPDATE
                     */
                    case "u" -> handleUpdate(
                            connection,
                            payload.get("after")
                    );

                    /*
                     * DELETE
                     */
                    case "d" -> handleDelete(
                            connection,
                            payload.get("before")
                    );

                    /*
                     * SNAPSHOT
                     */
                    case "r" -> System.out.println(
                            "Snapshot/read event ignored"
                    );

                    /*
                     * UNKNOWN
                     */
                    default -> {

                        System.out.println(
                                "Unsupported CDC operation: "
                                        + operation
                        );

                        return;
                    }
                }
            }

            /*
             * ================================
             * SUCCESS
             * ================================
             */

            long executionTime =
                    System.currentTimeMillis()
                            - startTime;

            System.out.println(
                    "CDC replay SUCCESS"
            );

            System.out.println(
                    "Execution time: "
                            + executionTime
                            + " ms"
            );

        } catch (Exception e) {

            /*
             * ================================
             * FAILURE
             * ================================
             */

            long executionTime =
                    System.currentTimeMillis()
                            - startTime;

            System.err.println(
                    "======================================"
            );

            System.err.println(
                    "CDC replay FAILED"
            );

            System.err.println(
                    "Execution time: "
                            + executionTime
                            + " ms"
            );

            System.err.println(
                    "Error: "
                            + e.getMessage()
            );

            System.err.println(
                    "======================================"
            );

            e.printStackTrace();
        }
    }

    /*
     * ================================
     * INSERT
     * ================================
     */

    private void handleInsert(
            Connection connection,
            JsonNode after) throws Exception {

        if (after == null
                || after.isNull()) {

            throw new IllegalArgumentException(
                    "INSERT event has no 'after' data"
            );
        }

        String sql = """
                INSERT INTO customers
                (id, name, email, created_at)
                VALUES (?, ?, ?, ?)
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    after.get("id").asLong()
            );

            statement.setString(
                    2,
                    after.get("name").asText()
            );

            statement.setString(
                    3,
                    after.get("email").asText()
            );

            setCreatedAt(
                    statement,
                    4,
                    after.get("created_at")
            );

            int rows =
                    statement.executeUpdate();

            System.out.println(
                    "INSERT replayed. Rows affected: "
                            + rows
            );
        }
    }

    /*
     * ================================
     * UPDATE
     * ================================
     */

    private void handleUpdate(
            Connection connection,
            JsonNode after) throws Exception {

        if (after == null
                || after.isNull()) {

            throw new IllegalArgumentException(
                    "UPDATE event has no 'after' data"
            );
        }

        String sql = """
                UPDATE customers
                SET name = ?,
                    email = ?,
                    created_at = ?
                WHERE id = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    after.get("name").asText()
            );

            statement.setString(
                    2,
                    after.get("email").asText()
            );

            setCreatedAt(
                    statement,
                    3,
                    after.get("created_at")
            );

            statement.setLong(
                    4,
                    after.get("id").asLong()
            );

            int rows =
                    statement.executeUpdate();

            System.out.println(
                    "UPDATE replayed. Rows affected: "
                            + rows
            );

            if (rows == 0) {

                System.out.println(
                        "WARNING: UPDATE affected 0 rows. "
                                + "Record may not exist in shadow DB."
                );
            }
        }
    }

    /*
     * ================================
     * DELETE
     * ================================
     */

    private void handleDelete(
            Connection connection,
            JsonNode before) throws Exception {

        if (before == null
                || before.isNull()) {

            throw new IllegalArgumentException(
                    "DELETE event has no 'before' data"
            );
        }

        String sql = """
                DELETE FROM customers
                WHERE id = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    before.get("id").asLong()
            );

            int rows =
                    statement.executeUpdate();

            System.out.println(
                    "DELETE replayed. Rows affected: "
                            + rows
            );

            if (rows == 0) {

                System.out.println(
                        "WARNING: DELETE affected 0 rows."
                );
            }
        }
    }

    /*
     * ================================
     * DEBEZIUM MICROTIMESTAMP
     * ================================
     *
     * Debezium:
     *
     * microseconds since Unix epoch
     *
     * PostgreSQL:
     *
     * Timestamp
     */

    private void setCreatedAt(
            PreparedStatement statement,
            int parameterIndex,
            JsonNode createdAt) throws Exception {

        if (createdAt == null
                || createdAt.isNull()) {

            statement.setTimestamp(
                    parameterIndex,
                    null
            );

            return;
        }

        long microseconds =
                createdAt.asLong();

        long milliseconds =
                microseconds / 1_000;

        int nanos =
                (int) (microseconds % 1_000) * 1_000;

        Timestamp timestamp =
                Timestamp.from(
                        Instant.ofEpochMilli(
                                milliseconds
                        )
                );

        timestamp.setNanos(
                timestamp.getNanos()
                        + nanos
        );

        statement.setTimestamp(
                parameterIndex,
                timestamp
        );
    }
}