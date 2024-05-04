package database;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Getter
public class DatabaseConnector {
    private final Connection connector;

    public DatabaseConnector(String connector, String host, String port, String user, String password, String dbName) throws SQLException {
        String dbUrl = "jdbc:" + connector + "://" + host + ":" + port + "/" + dbName;
        this.connector = DriverManager.getConnection(dbUrl, user, password);
    }

    public void close() throws SQLException {
        connector.close();
    }
}