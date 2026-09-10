package com.gauravlad.shadowbase_backend.dto;

public record CreateEnvironmentRequest(
        String name,
        String databaseType,
        String databaseVersion,
        String sourceDatabase,
        String sourceSchema
) {
}