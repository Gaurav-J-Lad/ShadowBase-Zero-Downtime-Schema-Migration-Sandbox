package com.gauravlad.shadowbase_backend.traffic;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "traffic_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrafficEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long environmentId;

    @Column(nullable = false)
    private String operationType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sql;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String status;

    private Long executionTimeMs;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}