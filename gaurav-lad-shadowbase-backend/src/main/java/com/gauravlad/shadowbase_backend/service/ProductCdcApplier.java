 package com.gauravlad.shadowbase_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gauravlad.shadowbase_backend.environment.ShadowDatabaseManager;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

@Service
public class ProductCdcApplier
        implements CdcTableApplier {

    private final ShadowDatabaseManager shadowDatabaseManager;

    public ProductCdcApplier(
            ShadowDatabaseManager shadowDatabaseManager) {

        this.shadowDatabaseManager =
                shadowDatabaseManager;
    }

    @Override
    public String getTableName() {
        return "products";
    }

    @Override
    public void applyInsert(
            Long environmentId,
            JsonNode data) {

        String sql = """
                INSERT INTO products
                (id, name, price, created_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """;

        try (
                Connection connection =
                        shadowDatabaseManager
                                .getConnection(environmentId);

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    getLong(data, "id")
            );

            statement.setString(
                    2,
                    getText(data, "name")
            );

            setDecimal(
                    statement,
                    3,
                    data,
                    "price"
            );

            setTimestamp(
                    statement,
                    4,
                    data,
                    "created_at"
            );

            int rows =
                    statement.executeUpdate();

            System.out.println(
                    "Product INSERT applied. "
                            + "Rows affected: "
                            + rows
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to apply Product INSERT CDC event",
                    e
            );
        }
    }

    @Override
    public void applyUpdate(
            Long environmentId,
            JsonNode data) {

        String sql = """
                UPDATE products
                SET
                    name = ?,
                    price = ?,
                    created_at = ?
                WHERE id = ?
                """;

        try (
                Connection connection =
                        shadowDatabaseManager
                                .getConnection(environmentId);

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    getText(data, "name")
            );

            setDecimal(
                    statement,
                    2,
                    data,
                    "price"
            );

            setTimestamp(
                    statement,
                    3,
                    data,
                    "created_at"
            );

            statement.setLong(
                    4,
                    getLong(data, "id")
            );

            int rows =
                    statement.executeUpdate();

            System.out.println(
                    "Product UPDATE applied. "
                            + "Rows affected: "
                            + rows
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to apply Product UPDATE CDC event",
                    e
            );
        }
    }

    @Override
    public void applyDelete(
            Long environmentId,
            JsonNode data) {

        String sql = """
                DELETE FROM products
                WHERE id = ?
                """;

        try (
                Connection connection =
                        shadowDatabaseManager
                                .getConnection(environmentId);

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    getLong(data, "id")
            );

            int rows =
                    statement.executeUpdate();

            System.out.println(
                    "Product DELETE applied. "
                            + "Rows affected: "
                            + rows
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to apply Product DELETE CDC event",
                    e
            );
        }
    }

    private String getText(
            JsonNode data,
            String field) {

        JsonNode node =
                data.get(field);

        if (node == null || node.isNull()) {
            return null;
        }

        return node.asText();
    }

    private Long getLong(
            JsonNode data,
            String field) {

        JsonNode node =
                data.get(field);

        if (node == null || node.isNull()) {

            throw new RuntimeException(
                    "CDC field missing: "
                            + field
            );
        }

        return node.asLong();
    }

    private void setDecimal(
            PreparedStatement statement,
            int parameterIndex,
            JsonNode data,
            String field)
            throws Exception {

        JsonNode node =
                data.get(field);

        if (node == null || node.isNull()) {

            statement.setBigDecimal(
                    parameterIndex,
                    null
            );

            return;
        }

        BigDecimal value =
                node.isNumber()
                        ? node.decimalValue()
                        : new BigDecimal(
                        node.asText()
                );

        statement.setBigDecimal(
                parameterIndex,
                value
        );
    }

    private void setTimestamp(
            PreparedStatement statement,
            int parameterIndex,
            JsonNode data,
            String field)
            throws Exception {

        JsonNode node =
                data.get(field);

        if (node == null || node.isNull()) {

            statement.setTimestamp(
                    parameterIndex,
                    null
            );

            return;
        }

        Timestamp timestamp =
                Timestamp.valueOf(
                        node.asText()
                                .replace(
                                        "T",
                                        " "
                                )
                );

        statement.setTimestamp(
                parameterIndex,
                timestamp
        );
    }
}
