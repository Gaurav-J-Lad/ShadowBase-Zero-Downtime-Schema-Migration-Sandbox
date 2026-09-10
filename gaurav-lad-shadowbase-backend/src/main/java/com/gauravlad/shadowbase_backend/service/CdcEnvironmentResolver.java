package com.gauravlad.shadowbase_backend.service;

import com.gauravlad.shadowbase_backend.entity.Environment;
import com.gauravlad.shadowbase_backend.repository.EnvironmentRepository;
import org.springframework.stereotype.Service;

@Service
public class CdcEnvironmentResolver {

    private final EnvironmentRepository environmentRepository;

    public CdcEnvironmentResolver(
            EnvironmentRepository environmentRepository) {

        this.environmentRepository =
                environmentRepository;
    }

    public Long resolveTargetEnvironment(
            String sourceDatabase,
            String sourceSchema) {

        return environmentRepository
                .findAll()
                .stream()
                .filter(this::isCdcReady)
                .filter(environment ->
                        matchesSource(
                                environment,
                                sourceDatabase,
                                sourceSchema
                        )
                )
                .findFirst()
                .map(Environment::getId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No CDC-ready environment found for "
                                        + "database="
                                        + sourceDatabase
                                        + ", schema="
                                        + sourceSchema
                        )
                );
    }

    private boolean isCdcReady(
            Environment environment) {

        String status =
                environment.getStatus();

        return "SYNCING".equalsIgnoreCase(status)
                || "RUNNING".equalsIgnoreCase(status);
    }

    private boolean matchesSource(
            Environment environment,
            String sourceDatabase,
            String sourceSchema) {

        boolean databaseMatches =
                sourceDatabase != null
                        && sourceDatabase.equals(
                        environment.getSourceDatabase()
                );

        boolean schemaMatches =
                sourceSchema == null
                        || sourceSchema.equals(
                        environment.getSourceSchema()
                );

        return databaseMatches && schemaMatches;
    }
}