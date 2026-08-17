package com.gauravlad.shadowbase_backend.traffic;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class TrafficGenerator {

    private final Random random = new Random();

    public TrafficEvent generateEvent(Long environmentId) {

        String[] operations = {
                "INSERT",
                "UPDATE",
                "DELETE"
        };

        String operation = operations[random.nextInt(operations.length)];

        String sql = generateSql(operation);

        return TrafficEvent.builder()
                .environmentId(environmentId)
                .operation(operation)
                .sql(sql)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private String generateSql(String operation) {

        long customerId = random.nextInt(100) + 1;

        return switch (operation) {

            case "INSERT" ->
                    "INSERT INTO customers (name, email) " +
                            "VALUES ('Customer" + customerId +
                            "', 'customer" + customerId + "@example.com');";

            case "UPDATE" ->
                    "UPDATE customers " +
                            "SET name = 'UpdatedCustomer" + customerId +
                            "' WHERE id = " + customerId + ";";

            case "DELETE" ->
                    "DELETE FROM customers " +
                            "WHERE id = " + customerId + ";";

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported operation: " + operation);
        };
    }

    public List<TrafficEvent> generateEvents(
            Long environmentId,
            int count) {

        return java.util.stream.IntStream
                .range(0, count)
                .mapToObj(i -> generateEvent(environmentId))
                .toList();
    }
}