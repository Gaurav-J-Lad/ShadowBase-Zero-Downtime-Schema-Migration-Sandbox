package com.gauravlad.shadowbase_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CdcEvent(

        @JsonProperty("op")
        String operation,

        JsonNode before,

        JsonNode after,

        CdcSource source
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CdcSource(

            String table,

            @JsonProperty("db")
            String database,

            String schema
    ) {
    }
}