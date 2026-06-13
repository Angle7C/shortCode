package com.atangle.shortcode.routing;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.Statement;

@Component
public class JdbcSchemaRoutingExecutor implements SchemaRoutingExecutor {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSchemaRoutingExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public <T> T executeOn(RouteTarget routeTarget, ConnectionCallback<T> action) {
        return jdbcTemplate.execute((ConnectionCallback<T>) connection -> {
            String originalSchema = readCurrentSchema(connection);
            switchSchema(connection, routeTarget.schemaName());
            try {
                return action.doInConnection(connection);
            } finally {
                restoreSchema(connection, originalSchema);
            }
        });
    }

    private String readCurrentSchema(Connection connection) {
        try {
            return connection.getSchema();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read current schema", ex);
        }
    }

    private void switchSchema(Connection connection, String schemaName) {
        executeStatement(connection, "SET search_path TO " + quoteIdentifier(schemaName));
    }

    private void restoreSchema(Connection connection, String originalSchema) {
        if (originalSchema == null || originalSchema.isBlank()) {
            executeStatement(connection, "RESET search_path");
            return;
        }
        executeStatement(connection, "SET search_path TO " + quoteIdentifier(originalSchema));
    }

    private void executeStatement(Connection connection, String sql) {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to execute SQL: " + sql, ex);
        }
    }

    private String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}
