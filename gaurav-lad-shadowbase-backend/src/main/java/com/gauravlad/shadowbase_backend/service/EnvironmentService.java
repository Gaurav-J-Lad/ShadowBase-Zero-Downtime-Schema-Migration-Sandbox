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
    private final ShadowDatabaseSnapshotService snapshotService;

    public EnvironmentService(
            EnvironmentRepository environmentRepository,
            ShadowDatabaseManager shadowDatabaseManager,
            ShadowDatabaseSchemaInitializer schemaInitializer,
            ShadowDatabaseSnapshotService snapshotService) {

        this.environmentRepository =
                environmentRepository;

        this.shadowDatabaseManager =
                shadowDatabaseManager;

        this.schemaInitializer =
                schemaInitializer;

        this.snapshotService =
                snapshotService;
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
                 * Step 1:
                 * Initialize shadow database schema.
                 */
                schemaInitializer.initialize(
                        container
                );

                System.out.println(
                        "Shadow database schema initialized"
                );

                /*
                 * Step 2:
                 * Store container ID.
                 */
                environment.setContainerId(
                        container.getContainerId()
                );

                environment =
                        environmentRepository.save(
                                environment
                        );

                /*
                 * Step 3:
                 * Copy existing production data
                 * into the shadow database.
                 */
                System.out.println(
                        "Starting initial database snapshot..."
                );

                snapshotService.createSnapshot(
                        environment.getId()
                );

                System.out.println(
                        "Initial database snapshot completed"
                );

                /*
                 * Step 4:
                 * Environment is ready.
                 */
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

    public Environment getEnvironmentById(
            Long id) {

        return environmentRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Environment not found with id: "
                                        + id
                        )
                );
    }

    public void deleteEnvironment(
            Long id) {

        Environment environment =
                getEnvironmentById(id);

        if (environment.getContainerId() != null) {

            shadowDatabaseManager.stopContainer(
                    id
            );
        }

        environmentRepository.delete(
                environment
        );
    }
}
