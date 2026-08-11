package com.gauravlad.shadowbase_backend.service;

import com.gauravlad.shadowbase_backend.dto.CreateEnvironmentRequest;
import com.gauravlad.shadowbase_backend.entity.Environment;
import com.gauravlad.shadowbase_backend.repository.EnvironmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnvironmentService {

    private final EnvironmentRepository environmentRepository;

    public EnvironmentService(EnvironmentRepository environmentRepository) {
        this.environmentRepository = environmentRepository;
    }

    public Environment createEnvironment(CreateEnvironmentRequest request) {

        Environment environment = Environment.builder()
                .name(request.name())
                .databaseType(request.databaseType())
                .databaseVersion(request.databaseVersion())
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();

        return environmentRepository.save(environment);
    }

    public List<Environment> getAllEnvironments() {
        return environmentRepository.findAll();
    }

    public Environment getEnvironmentById(Long id) {
        return environmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Environment not found with id: " + id));
    }

    public void deleteEnvironment(Long id) {
        Environment environment = getEnvironmentById(id);
        environmentRepository.delete(environment);
    }
}