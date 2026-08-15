package com.gauravlad.shadowbase_backend.service;

import com.gauravlad.shadowbase_backend.dto.CreateMigrationRequest;
import com.gauravlad.shadowbase_backend.entity.Environment;
import com.gauravlad.shadowbase_backend.entity.Migration;
import com.gauravlad.shadowbase_backend.repository.EnvironmentRepository;
import com.gauravlad.shadowbase_backend.repository.MigrationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MigrationService {

    private final MigrationRepository migrationRepository;
    private final EnvironmentRepository environmentRepository;

    public MigrationService(
            MigrationRepository migrationRepository,
            EnvironmentRepository environmentRepository) {

        this.migrationRepository = migrationRepository;
        this.environmentRepository = environmentRepository;
    }

    public Migration createMigration(CreateMigrationRequest request) {

        Environment environment = environmentRepository
                .findById(request.environmentId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Environment not found with id: "
                                        + request.environmentId()));

        Migration migration = Migration.builder()
                .environment(environment)
                .sqlScript(request.sqlScript())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        return migrationRepository.save(migration);
    }

    public List<Migration> getMigrationsByEnvironment(Long environmentId) {

        if (!environmentRepository.existsById(environmentId)) {
            throw new RuntimeException(
                    "Environment not found with id: " + environmentId);
        }

        return migrationRepository.findByEnvironmentId(environmentId);
    }

    public Migration getMigrationById(Long id) {

        return migrationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Migration not found with id: " + id));
    }
}