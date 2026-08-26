package com.gauravlad.shadowbase_backend.environment;

import org.springframework.stereotype.Component;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.Statement;

@Component
public class ShadowDatabaseSchemaInitializer {

    public void initialize(PostgreSQLContainer<?> container) {

        if (container == null) {
            throw new IllegalArgumentException(
                    "PostgreSQL container is null"
            );
        }

        if (!container.isRunning()) {
            throw new IllegalStateException(
                    "PostgreSQL container is not running"
            );
        }

        String jdbcUrl = container.getJdbcUrl();
        String username = container.getUsername();
        String password = container.getPassword();

        System.out.println(
                "Initializing shadow database schema..."
        );

        System.out.println(
                "JDBC URL: " + jdbcUrl
        );

        String sql = """
                CREATE TABLE IF NOT EXISTS customers (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(150) NOT NULL UNIQUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );

                CREATE TABLE IF NOT EXISTS products (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    price DECIMAL(10, 2) NOT NULL,
                    stock INTEGER NOT NULL
                );

                CREATE TABLE IF NOT EXISTS orders (
                    id BIGSERIAL PRIMARY KEY,
                    customer_id BIGINT NOT NULL,
                    product_id BIGINT NOT NULL,
                    quantity INTEGER NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                    CONSTRAINT fk_order_customer
                        FOREIGN KEY (customer_id)
                        REFERENCES customers(id),

                    CONSTRAINT fk_order_product
                        FOREIGN KEY (product_id)
                        REFERENCES products(id)
                );
                """;

        try (
                Connection connection =
                        container.createConnection("");

                Statement statement =
                        connection.createStatement()
        ) {

            statement.execute(sql);

            System.out.println(
                    "Shadow database schema initialized successfully."
            );

        } catch (Exception e) {

            System.err.println(
                    "Failed to initialize shadow database schema."
            );

            throw new RuntimeException(
                    "Failed to initialize shadow database schema",
                    e
            );
        }
    }
}