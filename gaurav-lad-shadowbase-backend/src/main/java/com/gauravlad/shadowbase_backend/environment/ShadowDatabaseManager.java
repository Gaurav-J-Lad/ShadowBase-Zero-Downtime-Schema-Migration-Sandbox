package com.gauravlad.shadowbase_backend.environment;

import com.gauravlad.shadowbase_backend.dto.ShadowDatabaseConnection;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ShadowDatabaseManager {

    private final Map<Long, PostgreSQLContainer<?>> containers =
            new ConcurrentHashMap<>();

    private final ShadowDatabaseSchemaInitializer schemaInitializer;

    public ShadowDatabaseManager(
            ShadowDatabaseSchemaInitializer schemaInitializer) {

        this.schemaInitializer = schemaInitializer;
    }

    /*
     * CREATE POSTGRES CONTAINER
     */
    public synchronized PostgreSQLContainer<?> createPostgresContainer(
            Long environmentId,
            String version) {

        stopContainer(environmentId);

        System.out.println(
                "Creating shadow PostgreSQL container for environment: "
                        + environmentId
        );

        PostgreSQLContainer<?> container =
                new PostgreSQLContainer<>("postgres:" + version)
                        .withDatabaseName("shadowdb")
                        .withUsername("postgres")
                        .withPassword("postgres")
                        .withEnv("TZ", "Asia/Kolkata");

        container.start();

        System.out.println(
                "Shadow PostgreSQL container started: "
                        + container.getContainerId()
        );

        /*
         * Initialize customers/products/orders.
         */
        schemaInitializer.initialize(container);

        /*
         * Store active container in application memory.
         */
        containers.put(
                environmentId,
                container
        );

        System.out.println();
        System.out.println(
                "======================================"
        );
        System.out.println(
                "SHADOW DATABASE READY"
        );
        System.out.println(
                "Environment ID : " + environmentId
        );
        System.out.println(
                "Container ID   : "
                        + container.getContainerId()
        );
        System.out.println(
                "JDBC URL       : "
                        + container.getJdbcUrl()
        );
        System.out.println(
                "======================================"
        );

        return container;
    }

    /*
     * GET ACTIVE CONTAINER
     */
    public PostgreSQLContainer<?> getContainer(
            Long environmentId) {

        PostgreSQLContainer<?> container =
                containers.get(environmentId);

        if (container == null) {
            return null;
        }

        if (!container.isRunning()) {

            containers.remove(environmentId);

            return null;
        }

        return container;
    }

    /*
     * GET CONTAINER OR FAIL
     */
    public PostgreSQLContainer<?> getRequiredContainer(
            Long environmentId) {

        PostgreSQLContainer<?> container =
                getContainer(environmentId);

        if (container == null) {

            throw new RuntimeException(
                    "No running shadow container found for environment: "
                            + environmentId
            );
        }

        return container;
    }

    /*
     * GET OR CREATE CONTAINER
     */
    public synchronized PostgreSQLContainer<?> getOrCreateContainer(
            Long environmentId,
            String version) {

        PostgreSQLContainer<?> container =
                getContainer(environmentId);

        if (container != null) {

            return container;
        }

        System.out.println(
                "No running shadow container found for environment "
                        + environmentId
                        + ". Creating a new one."
        );

        return createPostgresContainer(
                environmentId,
                version
        );
    }

    /*
     * GET JDBC CONNECTION
     */
    public Connection getConnection(
            Long environmentId) {

        PostgreSQLContainer<?> container =
                getRequiredContainer(environmentId);

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

    /*
     * GET JDBC URL
     *
     * IMPORTANT:
     * Testcontainers uses a dynamic host port.
     *
     * Example:
     * jdbc:postgresql://localhost:55894/shadowdb
     */
    public String getJdbcUrl(
            Long environmentId) {

        return getRequiredContainer(
                environmentId
        ).getJdbcUrl();
    }

    /*
     * GET USERNAME
     */
    public String getUsername(
            Long environmentId) {

        return getRequiredContainer(
                environmentId
        ).getUsername();
    }

    /*
     * GET PASSWORD
     */
    public String getPassword(
            Long environmentId) {

        return getRequiredContainer(
                environmentId
        ).getPassword();
    }

    /*
     * GET CONTAINER ID
     */
    public String getContainerId(
            Long environmentId) {

        return getRequiredContainer(
                environmentId
        ).getContainerId();
    }

    /*
     * GET CONNECTION DETAILS DTO
     */
    public ShadowDatabaseConnection getConnectionDetails(
            Long environmentId) {

        PostgreSQLContainer<?> container =
                getRequiredContainer(
                        environmentId
                );

        return new ShadowDatabaseConnection(
                container.getJdbcUrl(),
                container.getUsername(),
                container.getPassword()
        );
    }

    /*
     * VERIFY CONNECTION
     */
    public boolean verifyConnection(
            Long environmentId) {

        PostgreSQLContainer<?> container =
                getContainer(environmentId);

        if (container == null) {
            return false;
        }

        try (
                Connection connection =
                        container.createConnection("")
        ) {

            return connection.isValid(5);

        } catch (Exception e) {

            return false;
        }
    }

    /*
     * STOP CONTAINER
     */
    public synchronized void stopContainer(
            Long environmentId) {

        PostgreSQLContainer<?> container =
                containers.remove(environmentId);

        if (container == null) {
            return;
        }

        try {

            System.out.println(
                    "Stopping shadow container for environment: "
                            + environmentId
            );

            container.stop();

            System.out.println(
                    "Shadow container stopped for environment: "
                            + environmentId
            );

        } catch (Exception e) {

            System.err.println(
                    "Failed to stop shadow container for environment "
                            + environmentId
                            + ": "
                            + e.getMessage()
            );
        }
    }
}