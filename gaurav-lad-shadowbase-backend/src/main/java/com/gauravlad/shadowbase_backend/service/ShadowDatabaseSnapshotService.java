package com.gauravlad.shadowbase_backend.service;

import com.gauravlad.shadowbase_backend.environment.ShadowDatabaseManager;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Service
public class ShadowDatabaseSnapshotService {

    private final ShadowDatabaseManager shadowDatabaseManager;

    public ShadowDatabaseSnapshotService(
            ShadowDatabaseManager shadowDatabaseManager) {

        this.shadowDatabaseManager =
                shadowDatabaseManager;
    }

    public void copyCustomers(Long environmentId) {

        String sourceUrl =
                "jdbc:postgresql://localhost:5433/production_db";

        String sourceUsername = "postgres";
        String sourcePassword = "postgres";

        String selectSql = """
                SELECT
                    id,
                    name,
                    email,
                    created_at
                FROM customers
                """;

        String insertSql = """
                INSERT INTO customers
                (
                    id,
                    name,
                    email,
                    created_at
                )
                VALUES (?, ?, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """;

        try (
                Connection sourceConnection =
                        java.sql.DriverManager.getConnection(
                                sourceUrl,
                                sourceUsername,
                                sourcePassword
                        );

                Connection shadowConnection =
                        shadowDatabaseManager.getConnection(
                                environmentId
                        );

                PreparedStatement selectStatement =
                        sourceConnection.prepareStatement(
                                selectSql
                        );

                PreparedStatement insertStatement =
                        shadowConnection.prepareStatement(
                                insertSql
                        );

                ResultSet resultSet =
                        selectStatement.executeQuery()
        ) {

            int count = 0;

            while (resultSet.next()) {

                insertStatement.setLong(
                        1,
                        resultSet.getLong("id")
                );

                insertStatement.setString(
                        2,
                        resultSet.getString("name")
                );

                insertStatement.setString(
                        3,
                        resultSet.getString("email")
                );

                insertStatement.setTimestamp(
                        4,
                        resultSet.getTimestamp("created_at")
                );

                insertStatement.executeUpdate();

                count++;
            }

            System.out.println(
                    "Customer snapshot completed. Rows copied: "
                            + count
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to copy customer snapshot",
                    e
            );
        }
    }
}