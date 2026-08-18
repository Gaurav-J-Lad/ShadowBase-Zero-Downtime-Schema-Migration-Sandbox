package com.gauravlad.shadowbase_backend.dto;

public record TrafficReplayResult(
        Long environmentId,
        int totalEvents,
        int successfulEvents,
        int failedEvents,
        long totalExecutionTimeMs
) {
}