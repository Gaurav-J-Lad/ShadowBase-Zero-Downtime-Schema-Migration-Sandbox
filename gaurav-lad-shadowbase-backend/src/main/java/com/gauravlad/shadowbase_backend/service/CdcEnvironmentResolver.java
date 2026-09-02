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

    /*
     * Resolve the shadow environment that should
     * receive the CDC event.
     */
    public Long resolveTargetEnvironment() {

        return environmentRepository
                .findAll()
                .stream()
                .filter(environment ->
                        "RUNNING".equalsIgnoreCase(
                                environment.getStatus()
                        )
                )
                .findFirst()
                .map(Environment::getId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No RUNNING shadow environment found"
                        )
                );
    }
}