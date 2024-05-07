package database;

import utils.logging.LoggerUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private final String dbConnection;
    private final String host;
    private final String port;
    private final String user;
    private final String password;
    private final String dbName;

    private Connection connection;

    public DatabaseConnector(String dbConnection, String host, String port, String user, String password, String dbName) {
        this.dbConnection = dbConnection;
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.dbName = dbName;
    }

    public Connection databaseConnect() {
        try {
            String dbUrl = "jdbc:" + this.dbConnection + "://" + this.host + ":" + this.port + "/" + this.dbName;
            this.connection = DriverManager.getConnection(dbUrl, this.user, this.password);
            return connection;
        } catch (SQLException e) {
            LoggerUtil.logError("SQL exception:", e);
        }
        return null;
    }

    public Connection mySqlConnect() {
        try {
            String dbUrl = "jdbc:" + this.dbConnection + "://" + this.host + ":" + this.port + "/";
            this.connection = DriverManager.getConnection(dbUrl, this.user, this.password);
            return connection;
        } catch (SQLException e) {
            LoggerUtil.logError("SQL exception:", e);
        }
        return null;
    }

    public void close() throws SQLException {
        connection.close();
    }
}