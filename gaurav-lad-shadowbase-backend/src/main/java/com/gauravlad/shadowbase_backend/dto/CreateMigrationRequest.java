package com.gauravlad.shadowbase_backend.dto;

public record CreateMigrationRequest(
        Long environmentId,
        String sqlScript
) {
}