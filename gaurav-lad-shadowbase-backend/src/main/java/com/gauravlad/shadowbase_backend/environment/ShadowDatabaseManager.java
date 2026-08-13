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

    public PostgreSQLContainer<?> createPostgresContainer(
            Long environmentId,
            String version) {

        PostgreSQLContainer<?> container =
                new PostgreSQLContainer<>("postgres:" + version)
                        .withDatabaseName("shadowdb")
                        .withUsername("postgres")
                        .withPassword("postgres");

        container.start();

        containers.put(environmentId, container);

        return container;
    }

    public PostgreSQLContainer<?> getContainer(Long environmentId) {
        return containers.get(environmentId);
    }

    public void stopContainer(Long environmentId) {

        PostgreSQLContainer<?> container =
                containers.remove(environmentId);

        if (container != null) {
            container.stop();
        }
    }

    public String getJdbcUrl(Long environmentId) {

        PostgreSQLContainer<?> container =
                getContainer(environmentId);

        if (container == null) {
            throw new RuntimeException(
                    "Container not found for environment: " + environmentId);
        }

        return container.getJdbcUrl();
    }

    public String getUsername(Long environmentId) {

        PostgreSQLContainer<?> container =
                getContainer(environmentId);

        if (container == null) {
            throw new RuntimeException(
                    "Container not found for environment: " + environmentId);
        }

        return container.getUsername();
    }

    public String getPassword(Long environmentId) {

        PostgreSQLContainer<?> container =
                getContainer(environmentId);

        if (container == null) {
            throw new RuntimeException(
                    "Container not found for environment: " + environmentId);
        }

        return container.getPassword();
    }

    public ShadowDatabaseConnection getConnectionDetails(Long environmentId) {

        PostgreSQLContainer<?> container =
                getContainer(environmentId);

        if (container == null) {
            throw new RuntimeException(
                    "Container not found for environment: " + environmentId);
        }

        return new ShadowDatabaseConnection(
                container.getJdbcUrl(),
                container.getUsername(),
                container.getPassword()
        );
    }
}