package com.gauravlad.shadowbase_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gauravlad.shadowbase_backend.environment.ShadowDatabaseManager;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

@Service
public class OrderCdcApplier {

    private final ShadowDatabaseManager shadowDatabaseManager;

    public OrderCdcApplier(
            ShadowDatabaseManager shadowDatabaseManager) {

        this.shadowDatabaseManager =
                shadowDatabaseManager;
    }

    /*
     * INSERT
     */
    public void applyInsert(
            Long environmentId,
            JsonNode data) {

        String sql =
                """
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

            statement.setLong(
                    2,
                    getLong(data, "customer_id")
            );

            statement.setLong(
                    3,
                    getLong(data, "product_id")
            );

            statement.setInt(
                    4,
                    getInteger(data, "quantity")
            );

            setDecimal(
                    statement,
                    5,
                    data,
                    "total_amount"
            );

            setTimestamp(
                    statement,
                    6,
                    data,
                    "created_at"
            );

            int rows =
                    statement.executeUpdate();

            System.out.println(
                    "Order INSERT applied. Rows affected: "
                            + rows
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to apply Order INSERT CDC event",
                    e
            );
        }
    }

    /*
     * UPDATE
     */
    public void applyUpdate(
            Long environmentId,
            JsonNode data) {

        String sql =
                """
                UPDATE orders
                SET
                    customer_id = ?,
                    product_id = ?,
                    quantity = ?,
                    total_amount = ?,
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

            statement.setLong(
                    1,
                    getLong(data, "customer_id")
            );

            statement.setLong(
                    2,
                    getLong(data, "product_id")
            );

            statement.setInt(
                    3,
                    getInteger(data, "quantity")
            );

            setDecimal(
                    statement,
                    4,
                    data,
                    "total_amount"
            );

            setTimestamp(
                    statement,
                    5,
                    data,
                    "created_at"
            );

            statement.setLong(
                    6,
                    getLong(data, "id")
            );

            int rows =
                    statement.executeUpdate();

            System.out.println(
                    "Order UPDATE applied. Rows affected: "
                            + rows
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to apply Order UPDATE CDC event",
                    e
            );
        }
    }

    /*
     * DELETE
     */
    public void applyDelete(
            Long environmentId,
            JsonNode data) {

        String sql =
                """
                DELETE FROM orders
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
                    "Order DELETE applied. Rows affected: "
                            + rows
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to apply Order DELETE CDC event",
                    e
            );
        }
    }

    /*
     * Get Long
     */
    private Long getLong(
            JsonNode data,
            String field) {

        JsonNode node =
                data.get(field);

        if (node == null || node.isNull()) {

            throw new RuntimeException(
                    "CDC field missing: " + field
            );
        }

        return node.asLong();
    }

    /*
     * Get Integer
     */
    private Integer getInteger(
            JsonNode data,
            String field) {

        JsonNode node =
                data.get(field);

        if (node == null || node.isNull()) {

            throw new RuntimeException(
                    "CDC field missing: " + field
            );
        }

        return node.asInt();
    }

    /*
     * Set decimal
     */
    private void setDecimal(
            PreparedStatement statement,
            int parameterIndex,
            JsonNode data,
            String field) throws Exception {

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

    /*
     * Set timestamp
     */
    private void setTimestamp(
            PreparedStatement statement,
            int parameterIndex,
            JsonNode data,
            String field) throws Exception {

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
