package com.gauravlad.shadowbase_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "environments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Environment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String databaseType;

    @Column(nullable = false)
    private String databaseVersion;

    @Column(nullable = false)
    private String status;

    private String containerId;

    private LocalDateTime createdAt;

    private LocalDateTime destroyedAt;
}