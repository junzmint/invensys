package persistence.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private final Connection connector;

    public DatabaseConnector(String connector, String host, String port, String user, String password, String dbName) throws SQLException {
        String dbUrl = "jdbc:" + connector + "://" + host + ":" + port + "/" + dbName;
        this.connector = DriverManager.getConnection(dbUrl, user, password);
    }

    public Connection getConnector() {
        return connector;
    }
}