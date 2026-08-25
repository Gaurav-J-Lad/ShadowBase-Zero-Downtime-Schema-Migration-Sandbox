package com.gauravlad.shadowbase_backend.environment;

import org.springframework.stereotype.Component;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Component
public class ShadowDatabaseSchemaInitializer {

    public void initialize(
            PostgreSQLContainer<?> container) {

        String jdbcUrl =
                container.getJdbcUrl()
                        + "&options=-c%20TimeZone%3DUTC";

        String username =
                container.getUsername();

        String password =
                container.getPassword();

        String sql = """
                CREATE TABLE customers (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(150) NOT NULL UNIQUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );

                CREATE TABLE products (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    price DECIMAL(10, 2) NOT NULL,
                    stock INTEGER NOT NULL
                );

                CREATE TABLE orders (
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
                        DriverManager.getConnection(
                                jdbcUrl,
                                username,
                                password
                        );

                Statement statement =
                        connection.createStatement()
        ) {

            statement.execute(sql);

            System.out.println(
                    "Shadow database schema created successfully."
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to initialize shadow database schema",
                    e
            );
        }
    }
}