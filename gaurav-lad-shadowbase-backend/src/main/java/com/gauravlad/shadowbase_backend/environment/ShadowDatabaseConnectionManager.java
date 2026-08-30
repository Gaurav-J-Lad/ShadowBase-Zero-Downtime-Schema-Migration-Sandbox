package com.gauravlad.shadowbase_backend.environment;

import org.springframework.stereotype.Component;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;

@Component
public class ShadowDatabaseConnectionManager {

    private final ShadowDatabaseManager shadowDatabaseManager;

    public ShadowDatabaseConnectionManager(
            ShadowDatabaseManager shadowDatabaseManager) {

        this.shadowDatabaseManager = shadowDatabaseManager;
    }

    /**
     * Get a connection to an already-running
     * shadow database.
     */
    public Connection getConnection(
            Long environmentId) {

        PostgreSQLContainer<?> container =
                shadowDatabaseManager.getContainer(
                        environmentId
                );

        if (container == null) {

            throw new RuntimeException(
                    "Shadow container not found for environment: "
                            + environmentId
            );
        }

        if (!container.isRunning()) {

            throw new RuntimeException(
                    "Shadow container is not running for environment: "
                            + environmentId
            );
        }

        try {

            return container.createConnection("");

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to connect to shadow database "
                            + "for environment: "
                            + environmentId,
                    e
            );
        }
    }

    /**
     * Get the JDBC URL of the running
     * shadow database.
     */
    public String getJdbcUrl(
            Long environmentId) {

        PostgreSQLContainer<?> container =
                getContainer(environmentId);

        return container.getJdbcUrl();
    }

    /**
     * Get shadow database username.
     */
    public String getUsername(
            Long environmentId) {

        PostgreSQLContainer<?> container =
                getContainer(environmentId);

        return container.getUsername();
    }

    /**
     * Get shadow database password.
     */
    public String getPassword(
            Long environmentId) {

        PostgreSQLContainer<?> container =
                getContainer(environmentId);

        return container.getPassword();
    }

    /**
     * Internal validation method.
     */
    private PostgreSQLContainer<?> getContainer(
            Long environmentId) {

        PostgreSQLContainer<?> container =
                shadowDatabaseManager.getContainer(
                        environmentId
                );

        if (container == null) {

            throw new RuntimeException(
                    "Shadow container not found for environment: "
                            + environmentId
            );
        }

        if (!container.isRunning()) {

            throw new RuntimeException(
                    "Shadow container is not running for environment: "
                            + environmentId
            );
        }

        return container;
    }
}