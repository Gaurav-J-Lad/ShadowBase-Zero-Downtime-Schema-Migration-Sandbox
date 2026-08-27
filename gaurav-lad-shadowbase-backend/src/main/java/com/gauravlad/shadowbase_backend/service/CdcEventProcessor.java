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

            JsonNode operation = payload.get("op");
            JsonNode after = payload.get("after");
            JsonNode before = payload.get("before");

            if (operation == null) {
                System.out.println("CDC event has no operation");
                return;
            }

            String op = operation.asText();

            System.out.println("======================================");
            System.out.println("CDC EVENT RECEIVED");
            System.out.println("Operation : " + op);
            System.out.println("======================================");

            switch (op) {

                case "c":
                    handleInsert(after);
                    break;

                case "u":
                    handleUpdate(before, after);
                    break;

                case "d":
                    handleDelete(before);
                    break;

                default:
                    System.out.println("Unsupported CDC operation: " + op);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleInsert(JsonNode after) {

        System.out.println("CDC INSERT detected");

        if (after == null) {
            return;
        }

        System.out.println("ID    : " + after.get("id").asLong());
        System.out.println("Name  : " + after.get("name").asText());
        System.out.println("Email : " + after.get("email").asText());

        // TODO:
        // Execute INSERT into shadow database
    }

    private void handleUpdate(JsonNode before, JsonNode after) {

        System.out.println("CDC UPDATE detected");

        if (after == null) {
            return;
        }

        System.out.println("Updated ID    : " + after.get("id").asLong());
        System.out.println("Updated Name  : " + after.get("name").asText());
        System.out.println("Updated Email : " + after.get("email").asText());

        // TODO:
        // Execute UPDATE into shadow database
    }

    private void handleDelete(JsonNode before) {

        System.out.println("CDC DELETE detected");

        if (before == null) {
            return;
        }

        System.out.println("Deleted ID : " + before.get("id").asLong());

        // TODO:
        // Execute DELETE from shadow database
    }
}