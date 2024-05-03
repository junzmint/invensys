package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySqlConnector {
    private final Connection connector;

    public MySqlConnector(String connector, String host, String port, String user, String password) throws SQLException {
        String dbUrl = "jdbc:" + connector + "://" + host + ":" + port + "/";
        this.connector = DriverManager.getConnection(dbUrl, user, password);
    }

    public Connection getConnector() {
        return connector;
    }

    public void close() throws SQLException {
        connector.close();
    }
}