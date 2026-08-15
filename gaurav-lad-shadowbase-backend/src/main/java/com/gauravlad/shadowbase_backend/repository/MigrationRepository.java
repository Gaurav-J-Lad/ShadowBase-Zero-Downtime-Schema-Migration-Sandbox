package com.gauravlad.shadowbase_backend.repository;

import com.gauravlad.shadowbase_backend.entity.Migration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MigrationRepository extends JpaRepository<Migration, Long> {

    List<Migration> findByEnvironmentId(Long environmentId);
}