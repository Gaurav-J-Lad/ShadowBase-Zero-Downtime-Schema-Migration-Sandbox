package com.gauravlad.shadowbase_backend.environment;

import com.gauravlad.shadowbase_backend.dto.ShadowDatabaseConnection;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.PostgreSQLContainer;

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

    /**
     * Create and start a new PostgreSQL shadow container.
     */
    public synchronized PostgreSQLContainer<?> createPostgresContainer(
            Long environmentId,
            String version) {

        // Stop existing container for this environment
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

        // Start PostgreSQL Testcontainer
        container.start();

        System.out.println(
                "Shadow PostgreSQL container started: "
                        + container.getContainerId()
        );

        // Create required tables
        schemaInitializer.initialize(container);

        System.out.println(
                "Shadow database schema initialized for environment: "
                        + environmentId
        );

        // Store container in memory
        containers.put(environmentId, container);

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
                "Container ID   : " + container.getContainerId()
        );
        System.out.println(
                "JDBC URL       : " + container.getJdbcUrl()
        );
        System.out.println(
                "======================================"
        );

        return container;
    }

    /**
     * Return an existing running container.
     */
    public PostgreSQLContainer<?> getContainer(
            Long environmentId) {

        PostgreSQLContainer<?> container =
                containers.get(environmentId);

        if (container == null) {
            return null;
        }

        if (!container.isRunning()) {

            System.out.println(
                    "Container is no longer running for environment: "
                            + environmentId
            );

            containers.remove(environmentId);

            return null;
        }

        return container;
    }

    /**
     * Get an existing container or create a new one.
     *
     * This is important because the containers map is stored
     * only in application memory. After restarting Spring Boot,
     * the map becomes empty.
     */
    public synchronized PostgreSQLContainer<?> getOrCreateContainer(
            Long environmentId,
            String version) {

        PostgreSQLContainer<?> container =
                getContainer(environmentId);

        if (container != null) {

            System.out.println(
                    "Using existing shadow container for environment: "
                            + environmentId
            );

            return container;
        }

        System.out.println(
                "No running shadow container found for environment: "
                        + environmentId
        );

        System.out.println(
                "Creating a new shadow container..."
        );

        return createPostgresContainer(
                environmentId,
                version
        );
    }

    /**
     * Stop and remove a container from the map.
     */
    public synchronized void stopContainer(
            Long environmentId) {

        PostgreSQLContainer<?> container =
                containers.remove(environmentId);

        if (container != null) {

            try {

                System.out.println(
                        "Stopping shadow container for environment: "
                                + environmentId
                );

                container.stop();

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

    /**
     * Get JDBC URL.
     */
    public String getJdbcUrl(
            Long environmentId) {

        PostgreSQLContainer<?> container =
                getContainer(environmentId);

        if (container == null) {

            throw new RuntimeException(
                    "Container not found for environment: "
                            + environmentId
            );
        }

        return container.getJdbcUrl();
    }

    /**
     * Get username.
     */
    public String getUsername(
            Long environmentId) {

        PostgreSQLContainer<?> container =
                getContainer(environmentId);

        if (container == null) {

            throw new RuntimeException(
                    "Container not found for environment: "
                            + environmentId
            );
        }

        return container.getUsername();
    }

    /**
     * Get password.
     */
    public String getPassword(
            Long environmentId) {

        PostgreSQLContainer<?> container =
                getContainer(environmentId);

        if (container == null) {

            throw new RuntimeException(
                    "Container not found for environment: "
                            + environmentId
            );
        }

        return container.getPassword();
    }

    /**
     * Get complete shadow database connection details.
     */
    public ShadowDatabaseConnection getConnectionDetails(
            Long environmentId) {

        PostgreSQLContainer<?> container =
                getContainer(environmentId);

        if (container == null) {

            throw new RuntimeException(
                    "Container not found for environment: "
                            + environmentId
            );
        }

        return new ShadowDatabaseConnection(
                container.getJdbcUrl(),
                container.getUsername(),
                container.getPassword()
        );
    }

    /**
     * Check whether the shadow database is reachable.
     */
    public boolean verifyConnection(
            Long environmentId) {

        PostgreSQLContainer<?> container =
                getContainer(environmentId);

        if (container == null) {
            return false;
        }

        try (var connection =
                     container.createConnection("")) {

            return connection.isValid(5);

        } catch (Exception e) {

            return false;
        }
    }
}