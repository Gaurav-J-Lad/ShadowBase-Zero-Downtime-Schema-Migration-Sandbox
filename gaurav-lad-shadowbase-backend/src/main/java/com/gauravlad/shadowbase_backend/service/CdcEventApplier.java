package com.gauravlad.shadowbase_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gauravlad.shadowbase_backend.entity.Environment;
import com.gauravlad.shadowbase_backend.repository.EnvironmentRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

@Service
public class CdcEventApplier {

    private final EnvironmentRepository environmentRepository;

    public CdcEventApplier(
            EnvironmentRepository environmentRepository) {

        this.environmentRepository =
                environmentRepository;
    }

    /*
     * INSERT
     */
    public void applyInsert(
            Long environmentId,
            JsonNode data) {

        try {

            Environment environment =
                    getEnvironment(environmentId);

            String jdbcUrl =
                    getJdbcUrl(environment);

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
                            DriverManager.getConnection(
                                    jdbcUrl,
                                    "postgres",
                                    "postgres"
                            );

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
            }

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

        try {

            Environment environment =
                    getEnvironment(environmentId);

            String jdbcUrl =
                    getJdbcUrl(environment);

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
                            DriverManager.getConnection(
                                    jdbcUrl,
                                    "postgres",
                                    "postgres"
                            );

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
            }

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

        try {

            Environment environment =
                    getEnvironment(environmentId);

            String jdbcUrl =
                    getJdbcUrl(environment);

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
                            DriverManager.getConnection(
                                    jdbcUrl,
                                    "postgres",
                                    "postgres"
                            );

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
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to apply DELETE CDC event",
                    e
            );
        }
    }

    /*
     * Get environment
     */
    private Environment getEnvironment(
            Long environmentId) {

        return environmentRepository
                .findById(environmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Environment not found: "
                                        + environmentId
                        )
                );
    }

    /*
     * Build JDBC URL for the shadow container.
     *
     * Testcontainers exposes PostgreSQL on a
     * random host port, therefore we need to
     * obtain that port dynamically.
     *
     * The current implementation expects the
     * container port to be available through
     * ShadowDatabaseManager.
     */
    private String getJdbcUrl(
            Environment environment) {

        /*
         * TEMPORARY:
         *
         * Your current environment 28 was started
         * with a JDBC URL similar to:
         *
         * jdbc:postgresql://localhost:55894/shadowdb
         *
         * This must become dynamic because the
         * Testcontainers host port changes.
         */
        throw new UnsupportedOperationException(
                "Dynamic shadow JDBC URL is the next step"
        );
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
     * Set timestamp.
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

        statement.setObject(
                parameterIndex,
                java.sql.Timestamp.valueOf(
                        createdAt.replace("T", " ")
                )
        );
    }
}