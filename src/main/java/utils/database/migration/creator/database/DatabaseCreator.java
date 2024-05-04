package utils.database.migration.creator.database;

import utils.logging.LoggerUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseCreator {
    private final Connection connector;

    public DatabaseCreator(Connection connector) {
        this.connector = connector;
    }

    public String createDatabase(String databaseName) {
        String result = null;
        try {
            // Check whether database exists
            if (databaseExists(connector, databaseName)) {
                dropDatabase(connector, databaseName);
            }

            // Create database
            createDatabase(connector, databaseName);
            result = "Database created successfully";
        } catch (SQLException se) {
            // JDBC Exception
            LoggerUtil.logError("SQLException occurred while creating database: {}", se);
            result = "Failed to create database: " + se.getMessage();
        } finally {
            try {
                if (connector != null) connector.close();
            } catch (SQLException se) {
                LoggerUtil.logError("SQLException occurred while closing connection: {}", se);
                if (result == null) {
                    result = "Failed to close connection: " + se.getMessage();
                }
            }
        }
        return result;
    }

    private boolean databaseExists(Connection connector, String databaseName) throws SQLException {
        DatabaseMetaData metaData = connector.getMetaData();
        ResultSet resultSet = metaData.getCatalogs();
        boolean databaseExists = false;
        while (resultSet.next()) {
            String dbName = resultSet.getString(1);
            if (dbName.equalsIgnoreCase(databaseName)) {
                databaseExists = true;
                resultSet.close();
                break;
            }
        }
        return databaseExists;
    }

    private void dropDatabase(Connection connector, String databaseName) throws SQLException {
        connector.createStatement().executeUpdate("DROP DATABASE " + databaseName);
    }

    private void createDatabase(Connection connector, String databaseName) throws SQLException {
        connector.createStatement().executeUpdate("CREATE DATABASE " + databaseName);
    }
}
