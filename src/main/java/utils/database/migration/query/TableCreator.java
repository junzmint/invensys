package utils.database.migration.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class TableCreator {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final String user;
    private final String password;
    private final String connector;
    private final String host;
    private final String port;
    private final String dbName;

    public TableCreator(String user, String password, String connector, String host, String port, String dbName) {
        this.user = user;
        this.password = password;
        this.connector = connector;
        this.host = host;
        this.port = port;
        this.dbName = dbName;
    }

    public String create(List<String> createTableStmts) {
        String dbUrl = "jdbc:" + this.connector + "://" + this.host + ":" + this.port + "/" + this.dbName;
        Connection conn = null;
        Statement stmt = null;
        String result = null;

        try {
            // Connect to SQL server
            conn = DriverManager.getConnection(dbUrl, this.user, this.password);
            // Create Statement instance
            stmt = conn.createStatement();

            // Query
            for (String createTableStmt : createTableStmts) {
                stmt.executeUpdate(createTableStmt);
            }

            result = "Tables created successfully";
        } catch (SQLException se) {
            // JDBC Exception
            logger.error("SQLException occurred while creating tables: {}", se.getMessage());
            result = "Failed to create tables: " + se.getMessage();
        } catch (Exception e) {
            // Class.forName Exception
            logger.error("Exception occurred while creating tables: {}", e.getMessage());
            result = "Failed to create tables: " + e.getMessage();
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
