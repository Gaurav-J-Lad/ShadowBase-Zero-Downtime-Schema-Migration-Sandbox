package com.gauravlad.shadowbase_backend.controller;
import com.gauravlad.shadowbase_backend.dto.CreateEnvironmentRequest;
import com.gauravlad.shadowbase_backend.entity.Environment;
import com.gauravlad.shadowbase_backend.service.EnvironmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/environments")
public class EnvironmentController {

    private final EnvironmentService environmentService;

    public EnvironmentController(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Environment createEnvironment(
            @Valid @RequestBody CreateEnvironmentRequest request) {

        return environmentService.createEnvironment(request);
    }

    @GetMapping
    public List<Environment> getAllEnvironments() {
        return environmentService.getAllEnvironments();
    }

    @GetMapping("/{id}")
    public Environment getEnvironmentById(@PathVariable Long id) {
        return environmentService.getEnvironmentById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEnvironment(@PathVariable Long id) {
        environmentService.deleteEnvironment(id);
    }
}