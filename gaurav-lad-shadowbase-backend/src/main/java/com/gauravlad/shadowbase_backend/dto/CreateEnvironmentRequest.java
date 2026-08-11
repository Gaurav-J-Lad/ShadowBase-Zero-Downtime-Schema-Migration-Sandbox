package com.gauravlad.shadowbase_backend.dto;


import jakarta.validation.constraints.NotBlank;

public record CreateEnvironmentRequest(

        @NotBlank
        String name,

        @NotBlank
        String databaseType,

        @NotBlank
        String databaseVersion
) {
}