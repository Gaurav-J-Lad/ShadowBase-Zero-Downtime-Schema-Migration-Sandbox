package com.gauravlad.shadowbase_backend.service;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

@Service
public class CdcEventApplier {

    public void applyInsert(
            DataSource shadowDataSource,
            String tableName,
            String name,
            String email
    ) {

        String sql = """
                INSERT INTO customers (name, email)
                VALUES (?, ?)
                """;

        try (Connection connection = shadowDataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, email);

            statement.executeUpdate();

            System.out.println(
                    "CDC INSERT applied to shadow database: "
                            + name + " / " + email
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to apply CDC event to shadow database", e
            );
        }
    }
}