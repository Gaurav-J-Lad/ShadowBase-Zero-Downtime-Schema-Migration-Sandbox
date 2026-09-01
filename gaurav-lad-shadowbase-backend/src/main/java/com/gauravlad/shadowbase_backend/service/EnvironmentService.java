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

    /*
     * CREATE ENVIRONMENT
     */
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

        /*
         * Save first so that the environment
         * gets its database ID.
         */
        environment =
                environmentRepository.save(environment);

        try {

            /*
             * Currently we support PostgreSQL.
             */
            if ("POSTGRESQL".equalsIgnoreCase(
                    request.databaseType())) {

                System.out.println(
                        "Creating shadow PostgreSQL container "
                                + "for environment "
                                + environment.getId()
                );

                /*
                 * Create Testcontainers PostgreSQL.
                 */
                var container =
                        shadowDatabaseManager
                                .createPostgresContainer(
                                        environment.getId(),
                                        request.databaseVersion()
                                );

                System.out.println(
                        "Shadow PostgreSQL container started."
                );

                System.out.println(
                        "Container ID: "
                                + container.getContainerId()
                );

                /*
                 * IMPORTANT:
                 *
                 * Save the container ID immediately.
                 *
                 * This allows the environment to know
                 * which shadow database belongs to it.
                 */
                environment.setContainerId(
                        container.getContainerId()
                );

                environment =
                        environmentRepository.save(
                                environment
                        );

                /*
                 * Initialize shadow database schema.
                 *
                 * Tables:
                 *
                 * customers
                 * products
                 * orders
                 */
                System.out.println(
                        "Initializing shadow database schema..."
                );

                schemaInitializer.initialize(container);

                System.out.println(
                        "Shadow database schema initialized."
                );

                /*
                 * Environment is now ready.
                 */
                environment.setStatus(
                        "RUNNING"
                );

                environment =
                        environmentRepository.save(
                                environment
                        );

                System.out.println();
                System.out.println(
                        "======================================"
                );
                System.out.println(
                        "SHADOW DATABASE READY"
                );
                System.out.println(
                        "Environment ID : "
                                + environment.getId()
                );
                System.out.println(
                        "Container ID   : "
                                + environment.getContainerId()
                );
                System.out.println(
                        "Status         : "
                                + environment.getStatus()
                );
                System.out.println(
                        "======================================"
                );

            } else {

                /*
                 * For currently unsupported database types,
                 * keep the environment record RUNNING.
                 *
                 * We can add other database types later.
                 */
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
                    "======================================"
            );

            System.err.println(
                    "ENVIRONMENT CREATION FAILED"
            );

            System.err.println(
                    "Environment ID: "
                            + environment.getId()
            );

            System.err.println(
                    "======================================"
            );

            e.printStackTrace();

            /*
             * Cleanup Testcontainers container
             * if it was successfully created.
             */
            try {

                shadowDatabaseManager.stopContainer(
                        environment.getId()
                );

                System.out.println(
                        "Shadow container cleanup completed."
                );

            } catch (Exception cleanupException) {

                System.err.println(
                        "Failed to cleanup shadow container: "
                                + cleanupException.getMessage()
                );
            }

            /*
             * Keep the environment record.
             *
             * This is useful for debugging.
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

    /*
     * GET ALL ENVIRONMENTS
     */
    public List<Environment> getAllEnvironments() {

        return environmentRepository.findAll();
    }

    /*
     * GET ENVIRONMENT BY ID
     */
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

    /*
     * DELETE ENVIRONMENT
     */
    public void deleteEnvironment(Long id) {

        Environment environment =
                getEnvironmentById(id);

        /*
         * Stop Testcontainers container
         * before deleting the environment record.
         */
        if (environment.getContainerId() != null) {

            System.out.println(
                    "Stopping shadow container for environment "
                            + id
            );

            shadowDatabaseManager.stopContainer(id);
        }

        /*
         * Delete environment record.
         */
        environmentRepository.delete(
                environment
        );

        System.out.println(
                "Environment "
                        + id
                        + " deleted."
        );
    }
}