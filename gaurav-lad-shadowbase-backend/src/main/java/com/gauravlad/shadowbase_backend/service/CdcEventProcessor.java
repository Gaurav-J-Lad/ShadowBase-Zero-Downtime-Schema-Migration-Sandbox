package com.gauravlad.shadowbase_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gauravlad.shadowbase_backend.entity.Environment;
import com.gauravlad.shadowbase_backend.repository.EnvironmentRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

@Service
public class CdcEventProcessor {

    private final ObjectMapper objectMapper;
    private final EnvironmentRepository environmentRepository;

    public CdcEventProcessor(
            ObjectMapper objectMapper,
            EnvironmentRepository environmentRepository) {

        this.objectMapper = objectMapper;
        this.environmentRepository = environmentRepository;
    }

    public void process(String message) {

        try {

            JsonNode event = objectMapper.readTree(message);

            JsonNode payload = event.get("payload");

            if (payload == null) {
                System.out.println("CDC event has no payload");
                return;
            }

            String operation = payload.get("op").asText();

            System.out.println("CDC Operation: " + operation);

            switch (operation) {

                case "c":
                    insert(payload.get("after"));
                    break;

                case "u":
                    update(payload.get("after"));
                    break;

                case "d":
                    delete(payload.get("before"));
                    break;

                default:
                    System.out.println(
                            "Unsupported CDC operation: " + operation
                    );
            }

        } catch (Exception e) {

            System.err.println("Error processing CDC event");

            e.printStackTrace();
        }
    }

    private void insert(JsonNode data) {

        if (data == null) {
            return;
        }

        long id = data.get("id").asLong();
        String name = data.get("name").asText();
        String email = data.get("email").asText();

        System.out.println("CDC INSERT");
        System.out.println("ID    : " + id);
        System.out.println("Name  : " + name);
        System.out.println("Email : " + email);

        executeInsert(id, name, email);
    }

    private void update(JsonNode data) {

        if (data == null) {
            return;
        }

        long id = data.get("id").asLong();
        String name = data.get("name").asText();
        String email = data.get("email").asText();

        System.out.println("CDC UPDATE");
        System.out.println("ID    : " + id);
        System.out.println("Name  : " + name);
        System.out.println("Email : " + email);

        executeUpdate(id, name, email);
    }

    private void delete(JsonNode data) {

        if (data == null) {
            return;
        }

        long id = data.get("id").asLong();

        System.out.println("CDC DELETE");
        System.out.println("ID: " + id);

        executeDelete(id);
    }

    private Connection getShadowConnection() throws Exception {

        Environment environment =
                environmentRepository.findById(28L)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Environment 28 not found"
                                ));

        String containerId = environment.getContainerId();

        if (containerId == null) {
            throw new RuntimeException(
                    "Environment 28 has no shadow container"
            );
        }

        /*
         * For the current test we use the JDBC URL exposed by
         * Testcontainers.
         *
         * Environment 28 currently has:
         *
         * jdbc:postgresql://localhost:55894/shadowdb
         */

        String jdbcUrl =
                "jdbc:postgresql://localhost:55894/shadowdb";

        return DriverManager.getConnection(
                jdbcUrl,
                "postgres",
                "postgres"
        );
    }

    private void executeInsert(
            long id,
            String name,
            String email) {

        String sql = """
                INSERT INTO customers (id, name, email)
                VALUES (?, ?, ?)
                """;

        try (
                Connection connection = getShadowConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, id);
            statement.setString(2, name);
            statement.setString(3, email);

            statement.executeUpdate();

            System.out.println(
                    "CDC INSERT applied to shadow database"
            );

        } catch (Exception e) {

            System.err.println(
                    "Failed to apply INSERT to shadow database"
            );

            e.printStackTrace();
        }
    }

    private void executeUpdate(
            long id,
            String name,
            String email) {

        String sql = """
                UPDATE customers
                SET name = ?, email = ?
                WHERE id = ?
                """;

        try (
                Connection connection = getShadowConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setLong(3, id);

            statement.executeUpdate();

            System.out.println(
                    "CDC UPDATE applied to shadow database"
            );

        } catch (Exception e) {

            System.err.println(
                    "Failed to apply UPDATE to shadow database"
            );

            e.printStackTrace();
        }
    }

    private void executeDelete(long id) {

        String sql = """
                DELETE FROM customers
                WHERE id = ?
                """;

        try (
                Connection connection = getShadowConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, id);

            statement.executeUpdate();

            System.out.println(
                    "CDC DELETE applied to shadow database"
            );

        } catch (Exception e) {

            System.err.println(
                    "Failed to apply DELETE to shadow database"
            );

            e.printStackTrace();
        }
    }
}