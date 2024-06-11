package processor.component.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DatabaseConnectorTest {

    private static final String DB_CONNECTION = "mysql";
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";
    private static final String DB_NAME = "invensys";

    private DatabaseConnector databaseConnector;

    @BeforeEach
    void setUp() {
        try (MockedStatic<DatabaseConstants> mockedConstants = Mockito.mockStatic(DatabaseConstants.class)) {
            mockedConstants.when(DatabaseConstants::getDatabaseConnection).thenReturn(DB_CONNECTION);
            mockedConstants.when(DatabaseConstants::getDatabaseHost).thenReturn(DB_HOST);
            mockedConstants.when(DatabaseConstants::getDatabasePort).thenReturn(DB_PORT);
            mockedConstants.when(DatabaseConstants::getDatabaseUser).thenReturn(DB_USER);
            mockedConstants.when(DatabaseConstants::getDatabasePassword).thenReturn(DB_PASSWORD);
            mockedConstants.when(DatabaseConstants::getDatabaseName).thenReturn(DB_NAME);

            databaseConnector = DatabaseConnector.databaseConnectorFactory();
        }
    }

    @Test
    void testDatabaseConnectorFactory() {
        assertNotNull(databaseConnector);
    }

    @Test
    void testDatabaseConnect() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            String expectedDbUrl = "jdbc:" + DB_CONNECTION + "://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
            mockedDriverManager.when(() -> DriverManager.getConnection(expectedDbUrl, DB_USER, DB_PASSWORD)).thenReturn(mockConnection);

            Connection connection = databaseConnector.databaseConnect();
            assertNotNull(connection);
            assertEquals(mockConnection, connection);
        }
    }

    @Test
    void testDatabaseConnectSQLException() {
        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class);
             MockedStatic<DatabaseLogger> mockedLogger = Mockito.mockStatic(DatabaseLogger.class)) {
            String expectedDbUrl = "jdbc:" + DB_CONNECTION + "://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
            SQLException sqlException = new SQLException("Test SQL Exception");
            mockedDriverManager.when(() -> DriverManager.getConnection(expectedDbUrl, DB_USER, DB_PASSWORD)).thenThrow(sqlException);

            Connection connection = databaseConnector.databaseConnect();
            assertNull(connection);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseError(eq("SQL_EXCEPTION: "), eq(sqlException)));
        }
    }

    @Test
    void testMySqlConnect() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            String expectedDbUrl = "jdbc:" + DB_CONNECTION + "://" + DB_HOST + ":" + DB_PORT + "/";
            mockedDriverManager.when(() -> DriverManager.getConnection(expectedDbUrl, DB_USER, DB_PASSWORD)).thenReturn(mockConnection);

            Connection connection = databaseConnector.mySqlConnect();
            assertNotNull(connection);
            assertEquals(mockConnection, connection);
        }
    }

    @Test
    void testMySqlConnectSQLException() {
        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class);
             MockedStatic<DatabaseLogger> mockedLogger = Mockito.mockStatic(DatabaseLogger.class)) {
            String expectedDbUrl = "jdbc:" + DB_CONNECTION + "://" + DB_HOST + ":" + DB_PORT + "/";
            SQLException sqlException = new SQLException("Test SQL Exception");
            mockedDriverManager.when(() -> DriverManager.getConnection(expectedDbUrl, DB_USER, DB_PASSWORD)).thenThrow(sqlException);

            Connection connection = databaseConnector.mySqlConnect();
            assertNull(connection);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseError(eq("SQL_EXCEPTION: "), eq(sqlException)));
        }
    }

    @Test
    void testClose() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            String expectedDbUrl = "jdbc:" + DB_CONNECTION + "://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
            mockedDriverManager.when(() -> DriverManager.getConnection(expectedDbUrl, DB_USER, DB_PASSWORD)).thenReturn(mockConnection);

            databaseConnector.databaseConnect();
            databaseConnector.close();
            verify(mockConnection).close();
        }
    }
}
