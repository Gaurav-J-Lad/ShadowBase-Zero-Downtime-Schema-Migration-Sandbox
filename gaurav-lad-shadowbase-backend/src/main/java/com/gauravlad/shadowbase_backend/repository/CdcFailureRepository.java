package com.gauravlad.shadowbase_backend.repository;

import com.gauravlad.shadowbase_backend.entity.CdcFailure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CdcFailureRepository
        extends JpaRepository<CdcFailure, Long> {

    List<CdcFailure> findAllByOrderByFailedAtDesc();
}
