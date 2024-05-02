package utils.database.migration.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final String user;
    private final String password;
    private final String connector;
    private final String host;
    private final String port;

    public DatabaseInitializer(String user, String password, String connector, String host, String port) {
        this.user = user;
        this.password = password;
        this.connector = connector;
        this.host = host;
        this.port = port;
    }

    public String create(String databaseName) {
        String dbUrl = "jdbc:" + this.connector + "://" + this.host + ":" + this.port + "/";
        Connection conn = null;
        Statement stmt = null;
        String result = null;

        try {
            // Connect to SQL server
            conn = DriverManager.getConnection(dbUrl, this.user, this.password);
            // Create Statement instance
            stmt = conn.createStatement();
            // Check whether database is existed
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet resultSet = metaData.getCatalogs();
            boolean databaseExists = false;
            while (resultSet.next()) {
                String dbName = resultSet.getString(1);
                if (dbName.equalsIgnoreCase(databaseName)) {
                    databaseExists = true;
                    break;
                }
            }
            resultSet.close();

            // If existed, then drop
            if (databaseExists) {
                stmt.executeUpdate("DROP DATABASE " + databaseName);
            }

            // Create database
            stmt.executeUpdate("CREATE DATABASE " + databaseName);
            result = "Database created successfully";
        } catch (SQLException se) {
            // JDBC Exception
            logger.error("SQLException occurred while creating database: {}", se.getMessage());
            result = "Failed to create database: " + se.getMessage();
        } catch (Exception e) {
            // Class.forName Exception
            logger.error("Exception occurred while creating database: {}", e.getMessage());
            result = "Failed to create database: " + e.getMessage();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException se) {
                logger.error("SQLException occurred while closing connection: {}", se.getMessage());
                if (result == null) {
                    result = "Failed to close connection: " + se.getMessage();
                }
            }
            return result;
        }
    }
}
