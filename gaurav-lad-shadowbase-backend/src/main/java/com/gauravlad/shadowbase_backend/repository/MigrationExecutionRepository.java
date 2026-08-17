package com.gauravlad.shadowbase_backend.repository;

import com.gauravlad.shadowbase_backend.entity.MigrationExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MigrationExecutionRepository
        extends JpaRepository<MigrationExecution, Long> {

    List<MigrationExecution> findByMigrationIdOrderByExecutedAtDesc(Long migrationId);
}