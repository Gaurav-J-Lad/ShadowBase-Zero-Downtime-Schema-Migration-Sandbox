package com.gauravlad.shadowbase_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gauravlad.shadowbase_backend.environment
        .ShadowDatabaseConnectionManager;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

@Service
public class CdcEventApplier {

    private final ShadowDatabaseConnectionManager
            connectionManager;

    public CdcEventApplier(
            ShadowDatabaseConnectionManager connectionManager) {

        this.connectionManager =
                connectionManager;
    }

    public void applyInsert(
            Long environmentId,
            JsonNode after) {

        if (after == null || after.isNull()) {

            throw new IllegalArgumentException(
                    "INSERT CDC event has no after data"
            );
        }

        String sql = """
                INSERT INTO customers
                (id, name, email, created_at)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        connectionManager.getConnection(
                                environmentId
                        );

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
                    "CDC INSERT applied to environment "
                            + environmentId
                            + ". Rows affected: "
                            + rows
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to replay INSERT "
                            + "against environment "
                            + environmentId,
                    e
            );
        }
    }

    public void applyUpdate(
            Long environmentId,
            JsonNode after) {

        if (after == null || after.isNull()) {

            throw new IllegalArgumentException(
                    "UPDATE CDC event has no after data"
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
                Connection connection =
                        connectionManager.getConnection(
                                environmentId
                        );

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
                    "CDC UPDATE applied to environment "
                            + environmentId
                            + ". Rows affected: "
                            + rows
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to replay UPDATE "
                            + "against environment "
                            + environmentId,
                    e
            );
        }
    }

    public void applyDelete(
            Long environmentId,
            JsonNode before) {

        if (before == null || before.isNull()) {

            throw new IllegalArgumentException(
                    "DELETE CDC event has no before data"
            );
        }

        String sql = """
                DELETE FROM customers
                WHERE id = ?
                """;

        try (
                Connection connection =
                        connectionManager.getConnection(
                                environmentId
                        );

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
                    "CDC DELETE applied to environment "
                            + environmentId
                            + ". Rows affected: "
                            + rows
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to replay DELETE "
                            + "against environment "
                            + environmentId,
                    e
            );
        }
    }

    private void setCreatedAt(
            PreparedStatement statement,
            int index,
            JsonNode createdAt)
            throws Exception {

        if (createdAt == null
                || createdAt.isNull()) {

            statement.setTimestamp(
                    index,
                    null
            );

            return;
        }

        long microseconds =
                createdAt.asLong();

        long seconds =
                microseconds / 1_000_000;

        long remainingMicros =
                microseconds % 1_000_000;

        Instant instant =
                Instant.ofEpochSecond(
                        seconds,
                        remainingMicros * 1_000
                );

        statement.setTimestamp(
                index,
                Timestamp.from(instant)
        );
    }
}