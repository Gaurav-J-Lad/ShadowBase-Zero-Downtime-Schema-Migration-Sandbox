package com.gauravlad.shadowbase_backend.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.gauravlad.shadowbase_backend.environment.ShadowDatabaseManager;
import org.springframework.stereotype.Service;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.PreparedStatement;

@Service
public class CdcReplayService {

    private final ShadowDatabaseManager shadowDatabaseManager;

    public CdcReplayService(
            ShadowDatabaseManager shadowDatabaseManager) {

        this.shadowDatabaseManager = shadowDatabaseManager;
    }

    public void replay(JsonNode event) {

        JsonNode payload = event.get("payload");

        if (payload == null || payload.isNull()) {
            System.out.println("CDC payload is null");
            return;
        }

        String operation =
                payload.get("op").asText();

        JsonNode source =
                payload.get("source");

        String table =
                source.get("table").asText();

        System.out.println("CDC operation: " + operation);
        System.out.println("CDC table: " + table);

        /*
         * Currently we are handling the customers table.
         */
        if (!"customers".equalsIgnoreCase(table)) {

            System.out.println(
                    "Ignoring unsupported table: " + table
            );

            return;
        }

        /*
         * For now we use environment ID 17.
         *
         * We will make this dynamic later.
         */
        Long environmentId = 17L;

        PostgreSQLContainer<?> container =
                shadowDatabaseManager.getContainer(environmentId);

        if (container == null || !container.isRunning()) {

            System.out.println(
                    "Shadow container is not running for environment: "
                            + environmentId
            );

            return;
        }

        try (Connection connection =
                     container.createConnection("")) {

            switch (operation) {

                case "c" -> handleInsert(
                        connection,
                        payload.get("after")
                );

                case "u" -> handleUpdate(
                        connection,
                        payload.get("before"),
                        payload.get("after")
                );

                case "d" -> handleDelete(
                        connection,
                        payload.get("before")
                );

                default -> System.out.println(
                        "Unsupported CDC operation: " + operation
                );
            }

            System.out.println(
                    "CDC event replayed successfully"
            );

        } catch (Exception e) {

            System.err.println(
                    "CDC replay failed: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    private void handleInsert(
            Connection connection,
            JsonNode after) throws Exception {

        if (after == null || after.isNull()) {
            return;
        }

        String sql = """
                INSERT INTO customers
                (id, name, email, created_at)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

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

            if (after.get("created_at") == null
                    || after.get("created_at").isNull()) {

                statement.setObject(4, null);

            } else {

                statement.setLong(
                        4,
                        after.get("created_at").asLong()
                );
            }

            statement.executeUpdate();
        }
    }

    private void handleUpdate(
            Connection connection,
            JsonNode before,
            JsonNode after) throws Exception {

        if (after == null || after.isNull()) {
            return;
        }

        String sql = """
                UPDATE customers
                SET name = ?,
                    email = ?,
                    created_at = ?
                WHERE id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    after.get("name").asText()
            );

            statement.setString(
                    2,
                    after.get("email").asText()
            );

            if (after.get("created_at") == null
                    || after.get("created_at").isNull()) {

                statement.setObject(3, null);

            } else {

                statement.setLong(
                        3,
                        after.get("created_at").asLong()
                );
            }

            statement.setLong(
                    4,
                    after.get("id").asLong()
            );

            statement.executeUpdate();
        }
    }

    private void handleDelete(
            Connection connection,
            JsonNode before) throws Exception {

        if (before == null || before.isNull()) {
            return;
        }

        String sql = """
                DELETE FROM customers
                WHERE id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    before.get("id").asLong()
            );

            statement.executeUpdate();
        }
    }
}