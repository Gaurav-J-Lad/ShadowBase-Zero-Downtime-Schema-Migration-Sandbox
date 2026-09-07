package com.gauravlad.shadowbase_backend.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record CdcEvent(
        String operation,
        JsonNode before,
        JsonNode after,
        CdcSource source
) {

    public record CdcSource(
            String connector,
            String database,
            String schema,
            String table,
            Long tsMs
    ) {
    }
}
