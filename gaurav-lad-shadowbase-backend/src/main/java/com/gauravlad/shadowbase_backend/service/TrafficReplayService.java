package com.gauravlad.shadowbase_backend.service;

import com.gauravlad.shadowbase_backend.dto.TrafficReplayResult;
import com.gauravlad.shadowbase_backend.environment.ShadowDatabaseManager;
import com.gauravlad.shadowbase_backend.repository.EnvironmentRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

@Service
public class TrafficReplayService {

    private final TrafficSimulatorService trafficSimulatorService;
    private final ShadowDatabaseManager shadowDatabaseManager;
    private final EnvironmentRepository environmentRepository;

    public TrafficReplayService(
            TrafficSimulatorService trafficSimulatorService,
            ShadowDatabaseManager shadowDatabaseManager,
            EnvironmentRepository environmentRepository) {

        this.trafficSimulatorService = trafficSimulatorService;
        this.shadowDatabaseManager = shadowDatabaseManager;
        this.environmentRepository = environmentRepository;
    }

    public TrafficReplayResult replayTraffic(
            Long environmentId,
            int count) {

        if (!environmentRepository.existsById(environmentId)) {
            throw new RuntimeException(
                    "Environment not found with id: " + environmentId);
        }

        var container =
                shadowDatabaseManager.getContainer(environmentId);

        if (container == null || !container.isRunning()) {
            throw new RuntimeException(
                    "Shadow database container is not running "
                            + "for environment: " + environmentId);
        }

        List<TrafficEvent> events =
                trafficSimulatorService.generateTraffic(
                        environmentId,
                        count);

        int successfulEvents = 0;
        int failedEvents = 0;
        long totalExecutionTimeMs = 0;

        try (Connection connection =
                     container.createConnection("");
             Statement statement =
                     connection.createStatement()) {

            for (TrafficEvent event : events) {

                long startTime =
                        System.currentTimeMillis();

                try {

                    statement.execute(event.getSql());

                    long executionTime =
                            System.currentTimeMillis() - startTime;

                    event.setStatus("SUCCESS");
                    event.setExecutionTimeMs(executionTime);
                    event.setErrorMessage(null);

                    successfulEvents++;
                    totalExecutionTimeMs += executionTime;

                } catch (Exception e) {

                    long executionTime =
                            System.currentTimeMillis() - startTime;

                    event.setStatus("FAILED");
                    event.setExecutionTimeMs(executionTime);
                    event.setErrorMessage(e.getMessage());

                    failedEvents++;
                    totalExecutionTimeMs += executionTime;
                }

                trafficSimulatorService.save(event);
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to connect to shadow database",
                    e);
        }

        return new TrafficReplayResult(
                environmentId,
                events.size(),
                successfulEvents,
                failedEvents,
                totalExecutionTimeMs
        );
    }

    public List<TrafficEvent> getTrafficHistory(
            Long environmentId) {

        if (!environmentRepository.existsById(environmentId)) {
            throw new RuntimeException(
                    "Environment not found with id: "
                            + environmentId);
        }

        return trafficSimulatorService
                .getTrafficHistory(environmentId);
    }
}