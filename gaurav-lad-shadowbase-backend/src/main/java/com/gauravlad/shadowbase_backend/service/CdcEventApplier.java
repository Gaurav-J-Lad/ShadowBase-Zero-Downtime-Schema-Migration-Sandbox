 package com.gauravlad.shadowbase_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gauravlad.shadowbase_backend.environment.ShadowDatabaseManager;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

@Service
public class CdcEventApplier {

    private final ShadowDatabaseManager shadowDatabaseManager;

    public CdcEventApplier(
            ShadowDatabaseManager shadowDatabaseManager) {

        this.shadowDatabaseManager =
                shadowDatabaseManager;
    }

    /*
     * INSERT
     */
    public void applyInsert(
            Long environmentId,
            JsonNode data) {

        System.out.println(
                "Applying INSERT to shadow database"
        );

        String sql =
                """
                INSERT INTO customers
                (id, name, email, created_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """;

        try (
                Connection connection =
                        shadowDatabaseManager
                                .getConnection(environmentId);

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            setCustomerValues(
                    statement,
                    data
            );

            int rows =
                    statement.executeUpdate();

            System.out.println(
                    "INSERT applied. Rows affected: "
                            + rows
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to apply INSERT CDC event",
                    e
            );
        }
    }

    /*
     * UPDATE
     */
    public void applyUpdate(
            Long environmentId,
            JsonNode data) {

        System.out.println(
                "Applying UPDATE to shadow database"
        );

        String sql =
                """
                UPDATE customers
                SET
                    name = ?,
                    email = ?,
                    created_at = ?
                WHERE id = ?
                """;

        try (
                Connection connection =
                        shadowDatabaseManager
                                .getConnection(environmentId);

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    getText(data, "name")
            );

            statement.setString(
                    2,
                    getText(data, "email")
            );

            setTimestamp(
                    statement,
                    3,
                    data
            );

            statement.setLong(
                    4,
                    getLong(data, "id")
            );

            int rows =
                    statement.executeUpdate();

            System.out.println(
                    "UPDATE applied. Rows affected: "
                            + rows
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to apply UPDATE CDC event",
                    e
            );
        }
    }

    /*
     * DELETE
     */
    public void applyDelete(
            Long environmentId,
            JsonNode data) {

        System.out.println(
                "Applying DELETE to shadow database"
        );

        String sql =
                """
                DELETE FROM customers
                WHERE id = ?
                """;

        try (
                Connection connection =
                        shadowDatabaseManager
                                .getConnection(environmentId);

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    getLong(data, "id")
            );

            int rows =
                    statement.executeUpdate();

            System.out.println(
                    "DELETE applied. Rows affected: "
                            + rows
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to apply DELETE CDC event",
                    e
            );
        }
    }

    /*
     * Set INSERT values
     */
    private void setCustomerValues(
            PreparedStatement statement,
            JsonNode data) throws Exception {

        statement.setLong(
                1,
                getLong(data, "id")
        );

        statement.setString(
                2,
                getText(data, "name")
        );

        statement.setString(
                3,
                getText(data, "email")
        );

        setTimestamp(
                statement,
                4,
                data
        );
    }

    /*
     * Get String
     */
    private String getText(
            JsonNode data,
            String field) {

        JsonNode node =
                data.get(field);

        if (node == null || node.isNull()) {
            return null;
        }

        return node.asText();
    }

    /*
     * Get Long
     */
    private Long getLong(
            JsonNode data,
            String field) {

        JsonNode node =
                data.get(field);

        if (node == null || node.isNull()) {

            throw new RuntimeException(
                    "CDC field missing: " + field
            );
        }

        return node.asLong();
    }

    /*
     * Set timestamp
     */
    private void setTimestamp(
            PreparedStatement statement,
            int parameterIndex,
            JsonNode data) throws Exception {

        String createdAt =
                getText(
                        data,
                        "created_at"
                );

        if (createdAt == null) {

            statement.setTimestamp(
                    parameterIndex,
                    null
            );

            return;
        }

        /*
         * PostgreSQL / Debezium timestamp can
         * normally arrive as:
         *
         * 2026-09-02T14:30:00
         *
         * Convert it to:
         *
         * 2026-09-02 14:30:00
         */
        Timestamp timestamp =
                Timestamp.valueOf(
                        createdAt
                                .replace("T", " ")
                );

        statement.setTimestamp(
                parameterIndex,
                timestamp
        );
    }
}