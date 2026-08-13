package com.gauravlad.shadowbase_backend.service;

import com.gauravlad.shadowbase_backend.dto.CreateEnvironmentRequest;
import com.gauravlad.shadowbase_backend.entity.Environment;
import com.gauravlad.shadowbase_backend.environment.ShadowDatabaseManager;
import com.gauravlad.shadowbase_backend.repository.EnvironmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnvironmentService {

    private final EnvironmentRepository environmentRepository;
    private final ShadowDatabaseManager shadowDatabaseManager;

    public EnvironmentService(
            EnvironmentRepository environmentRepository,
            ShadowDatabaseManager shadowDatabaseManager) {

        this.environmentRepository = environmentRepository;
        this.shadowDatabaseManager = shadowDatabaseManager;
    }

    public Environment createEnvironment(CreateEnvironmentRequest request) {

        Environment environment = Environment.builder()
                .name(request.name())
                .databaseType(request.databaseType())
                .databaseVersion(request.databaseVersion())
                .status("CREATING")
                .createdAt(LocalDateTime.now())
                .build();

        environment = environmentRepository.save(environment);

        if ("POSTGRESQL".equalsIgnoreCase(request.databaseType())) {

            var container = shadowDatabaseManager.createPostgresContainer(
                    environment.getId(),
                    request.databaseVersion()
            );

            environment.setContainerId(container.getContainerId());
            environment.setStatus("RUNNING");

            environment = environmentRepository.save(environment);
        }

        return environment;
    }

    public List<Environment> getAllEnvironments() {
        return environmentRepository.findAll();
    }

    public Environment getEnvironmentById(Long id) {

        return environmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Environment not found with id: " + id));
    }

    public void deleteEnvironment(Long id) {

        Environment environment = getEnvironmentById(id);

        // Stop the Docker container before deleting the environment
        if (environment.getContainerId() != null) {
            shadowDatabaseManager.stopContainer(id);
        }

        // Delete the environment record from PostgreSQL
        environmentRepository.delete(environment);
    }
}