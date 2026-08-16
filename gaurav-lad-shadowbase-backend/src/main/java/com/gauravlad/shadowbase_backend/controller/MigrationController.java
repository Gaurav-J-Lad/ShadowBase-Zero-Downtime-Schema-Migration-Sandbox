package com.gauravlad.shadowbase_backend.controller;

import com.gauravlad.shadowbase_backend.dto.CreateMigrationRequest;
import com.gauravlad.shadowbase_backend.entity.Migration;
import com.gauravlad.shadowbase_backend.service.MigrationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/migrations")
public class MigrationController {

    private final MigrationService migrationService;

    public MigrationController(MigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Migration createMigration(
            @RequestBody CreateMigrationRequest request) {

        return migrationService.createMigration(request);
    }

    @GetMapping("/environment/{environmentId}")
    public List<Migration> getMigrationsByEnvironment(
            @PathVariable Long environmentId) {

        return migrationService.getMigrationsByEnvironment(environmentId);
    }

    @GetMapping("/{id}")
    public Migration getMigrationById(@PathVariable Long id) {

        return migrationService.getMigrationById(id);
    }

    @PostMapping("/{id}/execute")
    public Migration executeMigration(@PathVariable Long id) {

        return migrationService.executeMigration(id);
    }
}