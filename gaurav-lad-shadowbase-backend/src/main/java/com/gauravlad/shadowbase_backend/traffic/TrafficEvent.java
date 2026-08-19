package com.gauravlad.shadowbase_backend.traffic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrafficEvent {

    private Long environmentId;

    private String operationType;

    private String sql;

    private LocalDateTime createdAt;
}