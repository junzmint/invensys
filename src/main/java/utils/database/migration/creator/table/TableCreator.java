package utils.database.migration.creator.table;

import utils.logging.LoggerUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class TableCreator {
    private final Connection connector;

    public TableCreator(Connection connector) {
        this.connector = connector;
    }

    public String createTables(List<String> createTableStmts) {
        String result = null;
        Statement stmt = null;

        try {
            // Create Statement instance
            stmt = connector.createStatement();

            // Execute create table statements
            for (String createTableStmt : createTableStmts) {
                stmt.executeUpdate(createTableStmt);
            }

            result = "Tables created successfully";
        } catch (SQLException se) {
            // JDBC Exception
            LoggerUtil.logError("SQLException occurred while creating tables: {}", se);
            result = "Failed to create tables: " + se.getMessage();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connector != null) connector.close();
            } catch (SQLException se) {
                // Error closing connection
                LoggerUtil.logError("SQLException occurred while closing connection: {}", se);
                if (result == null) {
                    result = "Failed to close connection: " + se.getMessage();
                }
            }
            return result;
        }
    }
}
