package com.gauravlad.shadowbase_backend.repository;

import com.gauravlad.shadowbase_backend.traffic.TrafficEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrafficEventRepository
        extends JpaRepository<TrafficEvent, Long> {

    List<TrafficEvent> findByEnvironmentIdOrderByCreatedAtDesc(
            Long environmentId);
}