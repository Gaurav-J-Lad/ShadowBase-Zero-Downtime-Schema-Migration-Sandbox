package com.gauravlad.shadowbase_backend.service;

import com.gauravlad.shadowbase_backend.repository.TrafficEventRepository;
import com.gauravlad.shadowbase_backend.traffic.TrafficEvent;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class TrafficSimulatorService {

    private final TrafficEventRepository trafficEventRepository;

    private final Random random = new Random();

    public TrafficSimulatorService(
            TrafficEventRepository trafficEventRepository) {

        this.trafficEventRepository = trafficEventRepository;
    }

    public List<TrafficEvent> generateTraffic(
            Long environmentId,
            int count) {

        List<TrafficEvent> events = new ArrayList<>();

        for (int i = 0; i < count; i++) {

            int operation = random.nextInt(3);

            String sql;
            String operationType;

            if (operation == 0) {

                int customerId = random.nextInt(1000);

                sql = "INSERT INTO customers " +
                        "(name, email) VALUES " +
                        "('Customer " + customerId + "', " +
                        "'customer" + customerId +
                        "@example.com');";

                operationType = "INSERT";

            } else if (operation == 1) {

                sql = "UPDATE customers " +
                        "SET name = 'Updated Customer' " +
                        "WHERE id = " +
                        (random.nextInt(10) + 1) +
                        ";";

                operationType = "UPDATE";

            } else {

                sql = "DELETE FROM customers " +
                        "WHERE id = " +
                        (random.nextInt(10) + 1) +
                        ";";

                operationType = "DELETE";
            }

            TrafficEvent event = TrafficEvent.builder()
                    .environmentId(environmentId)
                    .operationType(operationType)
                    .sql(sql)
                    .createdAt(LocalDateTime.now())
                    .build();

            events.add(event);
        }

        return events;
    }

    public TrafficEvent save(TrafficEvent event) {

        return trafficEventRepository.save(event);
    }

    public List<TrafficEvent> getTrafficHistory(
            Long environmentId) {

        return trafficEventRepository
                .findByEnvironmentIdOrderByCreatedAtDesc(
                        environmentId);
    }
}