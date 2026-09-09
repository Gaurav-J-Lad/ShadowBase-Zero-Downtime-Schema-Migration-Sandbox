package com.gauravlad.shadowbase_backend.controller;

import com.gauravlad.shadowbase_backend.service.ShadowDatabaseSnapshotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/environments")
public class SnapshotController {

    private final ShadowDatabaseSnapshotService snapshotService;

    public SnapshotController(
            ShadowDatabaseSnapshotService snapshotService) {

        this.snapshotService = snapshotService;
    }

    @PostMapping("/{environmentId}/snapshot")
    public ResponseEntity<String> createSnapshot(
            @PathVariable Long environmentId) {

        snapshotService.copyCustomers(environmentId);

        return ResponseEntity.ok(
                "Customer snapshot completed successfully "
                        + "for environment "
                        + environmentId
        );
    }
}
