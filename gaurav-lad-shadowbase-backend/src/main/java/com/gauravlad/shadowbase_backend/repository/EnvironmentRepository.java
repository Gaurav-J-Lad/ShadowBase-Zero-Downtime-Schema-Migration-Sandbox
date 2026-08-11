package com.gauravlad.shadowbase_backend.repository;

import com.gauravlad.shadowbase_backend.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvironmentRepository extends JpaRepository<Environment, Long> {
}