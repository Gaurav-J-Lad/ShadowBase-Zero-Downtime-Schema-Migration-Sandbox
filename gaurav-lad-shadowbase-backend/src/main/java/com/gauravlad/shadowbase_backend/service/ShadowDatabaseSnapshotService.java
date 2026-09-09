 package com.gauravlad.shadowbase_backend.service;

import com.gauravlad.shadowbase_backend.environment.ShadowDatabaseManager;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Service
public class ShadowDatabaseSnapshotService {

    private static final String SOURCE_URL =
            "jdbc:postgresql://localhost:5433/production_db";

    private static final String SOURCE_USERNAME =
            "postgres";

    private static final String SOURCE_PASSWORD =
            "postgres";

    private final ShadowDatabaseManager shadowDatabaseManager;

    public ShadowDatabaseSnapshotService(
            ShadowDatabaseManager shadowDatabaseManager) {

        this.shadowDatabaseManager =
                shadowDatabaseManager;
    }

    public void createSnapshot(Long environmentId) {

        System.out.println(
                "========================================"
        );

        System.out.println(
                "Starting shadow database snapshot"
        );

        System.out.println(
                "Environment ID: " + environmentId
        );

        try (
                Connection sourceConnection =
                        DriverManager.getConnection(
                                SOURCE_URL,
                                SOURCE_USERNAME,
                                SOURCE_PASSWORD
                        );

                Connection shadowConnection =
                        shadowDatabaseManager.getConnection(
                                environmentId
                        )
        ) {

            shadowConnection.setAutoCommit(false);

            copyCustomers(
                    sourceConnection,
                    shadowConnection
            );

            copyProducts(
                    sourceConnection,
                    shadowConnection
            );

            copyOrders(
                    sourceConnection,
                    shadowConnection
            );

            shadowConnection.commit();

            System.out.println(
                    "Shadow database snapshot completed successfully"
            );

        } catch (Exception e) {

            System.err.println(
                    "Shadow database snapshot failed"
            );

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to create shadow database snapshot",
                    e
            );
        }

        System.out.println(
                "========================================"
        );
    }

    private void copyCustomers(
            Connection sourceConnection,
            Connection shadowConnection)
            throws Exception {

        String selectSql = """
                SELECT
                    id,
                    name,
                    email,
                    created_at
                FROM customers
                ORDER BY id
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

        int count = 0;

        try (
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
        }

        System.out.println(
                "Customers copied: " + count
        );
    }

    private void copyProducts(
            Connection sourceConnection,
            Connection shadowConnection)
            throws Exception {

        String selectSql = """
                SELECT
                    id,
                    name,
                    price,
                    created_at
                FROM products
                ORDER BY id
                """;

        String insertSql = """
                INSERT INTO products
                (
                    id,
                    name,
                    price,
                    created_at
                )
                VALUES (?, ?, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """;

        int count = 0;

        try (
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

            while (resultSet.next()) {

                insertStatement.setLong(
                        1,
                        resultSet.getLong("id")
                );

                insertStatement.setString(
                        2,
                        resultSet.getString("name")
                );

                insertStatement.setBigDecimal(
                        3,
                        resultSet.getBigDecimal("price")
                );

                insertStatement.setTimestamp(
                        4,
                        resultSet.getTimestamp("created_at")
                );

                insertStatement.executeUpdate();

                count++;
            }
        }

        System.out.println(
                "Products copied: " + count
        );
    }

    private void copyOrders(
            Connection sourceConnection,
            Connection shadowConnection)
            throws Exception {

        String selectSql = """
                SELECT
                    id,
                    customer_id,
                    product_id,
                    quantity,
                    total_amount,
                    created_at
                FROM orders
                ORDER BY id
                """;

        String insertSql = """
                INSERT INTO orders
                (
                    id,
                    customer_id,
                    product_id,
                    quantity,
                    total_amount,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """;

        int count = 0;

        try (
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

            while (resultSet.next()) {

                insertStatement.setLong(
                        1,
                        resultSet.getLong("id")
                );

                insertStatement.setLong(
                        2,
                        resultSet.getLong("customer_id")
                );

                insertStatement.setLong(
                        3,
                        resultSet.getLong("product_id")
                );

                insertStatement.setInt(
                        4,
                        resultSet.getInt("quantity")
                );

                insertStatement.setBigDecimal(
                        5,
                        resultSet.getBigDecimal("total_amount")
                );

                insertStatement.setTimestamp(
                        6,
                        resultSet.getTimestamp("created_at")
                );

                insertStatement.executeUpdate();

                count++;
            }
        }

        System.out.println(
                "Orders copied: " + count
        );
    }
}
