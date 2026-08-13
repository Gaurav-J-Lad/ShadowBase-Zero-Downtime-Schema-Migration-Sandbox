package com.gauravlad.shadowbase_backend.dto;

public record ShadowDatabaseConnection(
        String jdbcUrl,
        String username,
        String password
) {
}