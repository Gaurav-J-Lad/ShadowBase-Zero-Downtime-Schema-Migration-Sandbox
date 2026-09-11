package com.gauravlad.shadowbase_backend.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface CdcTableApplier {

    String getTableName();

    void applyInsert(
            Long environmentId,
            JsonNode data
    );

    void applyUpdate(
            Long environmentId,
            JsonNode data
    );

    void applyDelete(
            Long environmentId,
            JsonNode data
    );
}
