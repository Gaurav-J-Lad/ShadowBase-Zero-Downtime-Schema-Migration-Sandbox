package com.gauravlad.shadowbase_backend.service;

import com.gauravlad.shadowbase_backend.dto.CreateEnvironmentRequest;
import com.gauravlad.shadowbase_backend.entity.Environment;
import com.gauravlad.shadowbase_backend.environment.ShadowDatabaseManager;
import com.gauravlad.shadowbase_backend.environment.ShadowDatabaseSchemaInitializer;
import com.gauravlad.shadowbase_backend.repository.EnvironmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnvironmentService {

    private final EnvironmentRepository environmentRepository;
    private final ShadowDatabaseManager shadowDatabaseManager;
    private final ShadowDatabaseSchemaInitializer schemaInitializer;

    public EnvironmentService(
            EnvironmentRepository environmentRepository,
            ShadowDatabaseManager shadowDatabaseManager,
            ShadowDatabaseSchemaInitializer schemaInitializer) {

        this.environmentRepository = environmentRepository;
        this.shadowDatabaseManager = shadowDatabaseManager;
        this.schemaInitializer = schemaInitializer;
    }

    public Environment createEnvironment(
            CreateEnvironmentRequest request) {

        Environment environment =
                Environment.builder()
                        .name(request.name())
                        .databaseType(request.databaseType())
                        .databaseVersion(request.databaseVersion())
                        .status("CREATING")
                        .createdAt(LocalDateTime.now())
                        .build();

        environment =
                environmentRepository.save(environment);

        try {

            if ("POSTGRESQL".equalsIgnoreCase(
                    request.databaseType())) {

                System.out.println(
                        "Creating shadow PostgreSQL container "
                                + "for environment "
                                + environment.getId()
                );

                var container =
                        shadowDatabaseManager
                                .createPostgresContainer(
                                        environment.getId(),
                                        request.databaseVersion()
                                );

                System.out.println(
                        "Shadow container started: "
                                + container.getContainerId()
                );

                /*
                 * Initialize customers,
                 * products and orders tables.
                 */
                schemaInitializer.initialize(container);

                environment.setContainerId(
                        container.getContainerId()
                );

                environment.setStatus(
                        "RUNNING"
                );

                environment =
                        environmentRepository.save(
                                environment
                        );

                System.out.println(
                        "Environment "
                                + environment.getId()
                                + " is RUNNING"
                );

            } else {

                environment.setStatus(
                        "RUNNING"
                );

                environment =
                        environmentRepository.save(
                                environment
                        );
            }

            return environment;

        } catch (Exception e) {

            System.err.println(
                    "Environment creation failed for ID "
                            + environment.getId()
            );

            e.printStackTrace();

            /*
             * Stop the Testcontainer if it was created
             * but schema initialization failed.
             */
            try {

                shadowDatabaseManager.stopContainer(
                        environment.getId()
                );

            } catch (Exception cleanupException) {

                System.err.println(
                        "Failed to cleanup shadow container: "
                                + cleanupException.getMessage()
                );
            }

            /*
             * Keep the database record so we can see
             * that creation failed.
             */
            environment.setStatus(
                    "FAILED"
            );

            environment =
                    environmentRepository.save(
                            environment
                    );

            throw new RuntimeException(
                    "Failed to create shadow environment",
                    e
            );
        }
    }

    public List<Environment> getAllEnvironments() {

        return environmentRepository.findAll();
    }

    public Environment getEnvironmentById(Long id) {

        return environmentRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Environment not found with id: "
                                        + id
                        )
                );
    }

    public void deleteEnvironment(Long id) {

        Environment environment =
                getEnvironmentById(id);

        if (environment.getContainerId() != null) {

            shadowDatabaseManager.stopContainer(id);
        }

        environmentRepository.delete(
                environment
        );
    }
}