package database;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DatabaseQueryExecutorTest {

    private Connection mockConnection;
    private DatabaseQueryExecutor databaseQueryExecutor;

    @BeforeEach
    void setUp() {
        mockConnection = mock(Connection.class);
        databaseQueryExecutor = new DatabaseQueryExecutor(mockConnection);
    }

    @Test
    void testConstructorInitialization() throws NoSuchFieldException, IllegalAccessException {
        // Use reflection to access the private 'connection' field
        Field connectionField = DatabaseQueryExecutor.class.getDeclaredField("connection");
        connectionField.setAccessible(true);

        // Verify that the connection field is correctly initialized
        assertEquals(mockConnection, connectionField.get(databaseQueryExecutor));
    }

    @Test
    void testDropDatabaseSuccess() throws Exception {
        // Mock PreparedStatement
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Use reflection to access and invoke the private method
            Method dropDatabaseMethod = DatabaseQueryExecutor.class.getDeclaredMethod("dropDatabase", String.class);
            dropDatabaseMethod.setAccessible(true);

            // Execute the method
            Assertions.assertDoesNotThrow(() -> dropDatabaseMethod.invoke(databaseQueryExecutor, "testDB"));

            // Verify that the SQL statement was prepared and executed
            verify(mockConnection).prepareStatement("DROP DATABASE IF EXISTS testDB");
            verify(mockStatement).executeUpdate();
            verify(mockStatement).close();

            // Verify logging
            ArgumentCaptor<StackTraceElement[]> stackTraceCaptor = ArgumentCaptor.forClass(StackTraceElement[].class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseInfo(eq("DB_DROPPED"), stackTraceCaptor.capture()));
        }
    }

    @Test
    void testDropDatabaseSQLException() throws Exception {
        // Mock PreparedStatement to throw SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test SQLException"));

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Use reflection to access and invoke the private method
            Method dropDatabaseMethod = DatabaseQueryExecutor.class.getDeclaredMethod("dropDatabase", String.class);
            dropDatabaseMethod.setAccessible(true);

            // Execute the method and verify that no exception is thrown
            Assertions.assertDoesNotThrow(() -> dropDatabaseMethod.invoke(databaseQueryExecutor, "testDB"));

            // Verify logging
            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseError(eq("SQL_EXCEPTION: "), exceptionCaptor.capture()));
            assertEquals("Test SQLException", exceptionCaptor.getValue().getMessage());
        }
    }

    @Test
    void testCreateDatabaseSuccess() throws Exception {
        // Mock PreparedStatement
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Use reflection to access and invoke the private method
            Method createDatabaseMethod = DatabaseQueryExecutor.class.getDeclaredMethod("createDatabase", String.class);
            createDatabaseMethod.setAccessible(true);

            // Execute the method
            Assertions.assertDoesNotThrow(() -> createDatabaseMethod.invoke(databaseQueryExecutor, "testDB"));

            // Verify that the SQL statement was prepared and executed
            verify(mockConnection).prepareStatement("CREATE DATABASE testDB");
            verify(mockStatement).executeUpdate();
            verify(mockStatement).close();

            // Verify logging
            ArgumentCaptor<StackTraceElement[]> stackTraceCaptor = ArgumentCaptor.forClass(StackTraceElement[].class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseInfo(eq("DB_CREATED"), stackTraceCaptor.capture()));
        }
    }

    @Test
    void testCreateDatabaseSQLException() throws Exception {
        // Mock PreparedStatement to throw SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test SQLException"));

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Use reflection to access and invoke the private method
            Method createDatabaseMethod = DatabaseQueryExecutor.class.getDeclaredMethod("createDatabase", String.class);
            createDatabaseMethod.setAccessible(true);

            // Execute the method and verify that no exception is thrown
            Assertions.assertDoesNotThrow(() -> createDatabaseMethod.invoke(databaseQueryExecutor, "testDB"));

            // Verify logging
            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseError(eq("SQL_EXCEPTION: "), exceptionCaptor.capture()));
            assertEquals("Test SQLException", exceptionCaptor.getValue().getMessage());
        }
    }

    @Test
    void testDropAndCreateDatabase() throws SQLException {
        // Mock PreparedStatement
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Call the dropAndCreateDatabase method
            databaseQueryExecutor.dropAndCreateDatabase("testDB");

            // Use inOrder to verify the calls in order
            InOrder inOrder = inOrder(mockConnection, mockStatement);

            // Verify that both dropDatabase and createDatabase methods are called
            inOrder.verify(mockConnection).prepareStatement("DROP DATABASE IF EXISTS testDB");
            inOrder.verify(mockStatement).executeUpdate();
            inOrder.verify(mockStatement).close();
            inOrder.verify(mockConnection).prepareStatement("CREATE DATABASE testDB");
            inOrder.verify(mockStatement).executeUpdate();
            inOrder.verify(mockStatement).close();

            // Verify logging for both methods
            ArgumentCaptor<StackTraceElement[]> stackTraceCaptor = ArgumentCaptor.forClass(StackTraceElement[].class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseInfo(eq("DB_DROPPED"), stackTraceCaptor.capture()));
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseInfo(eq("DB_CREATED"), stackTraceCaptor.capture()));
        }
    }

    @Test
    void testDropAndCreateDatabaseSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test SQLException"));

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            assertDoesNotThrow(() -> databaseQueryExecutor.dropAndCreateDatabase("testDB"));

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseError(eq("SQL_EXCEPTION: "), exceptionCaptor.capture()), times(2));
            assertEquals("Test SQLException", exceptionCaptor.getAllValues().get(0).getMessage());
            assertEquals("Test SQLException", exceptionCaptor.getAllValues().get(1).getMessage());
        }
    }

    @Test
    void testCreateTableSuccess() throws SQLException {
        Statement mockStatement = mock(Statement.class);
        when(mockConnection.createStatement()).thenReturn(mockStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            assertDoesNotThrow(() -> databaseQueryExecutor.createTable("CREATE TABLE test (id INT)", "test"));

            verify(mockConnection).createStatement();
            verify(mockStatement).executeUpdate("CREATE TABLE test (id INT)");
            verify(mockStatement).close();

            ArgumentCaptor<StackTraceElement[]> stackTraceCaptor = ArgumentCaptor.forClass(StackTraceElement[].class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseInfo(eq("TABLE_CREATED: test"), stackTraceCaptor.capture()));
        }
    }

    @Test
    void testCreateTableSQLException() throws SQLException {
        when(mockConnection.createStatement()).thenThrow(new SQLException("Test SQLException"));

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            assertDoesNotThrow(() -> databaseQueryExecutor.createTable("CREATE TABLE test (id INT)", "test"));

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseError(eq("SQL_EXCEPTION: "), exceptionCaptor.capture()));
            assertEquals("Test SQLException", exceptionCaptor.getValue().getMessage());
        }
    }

    @Test
    void testInsertOffsetTableSuccess() throws SQLException {
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        assertDoesNotThrow(() -> databaseQueryExecutor.insertOffsetTable("testId", 123L));

        verify(mockConnection).prepareStatement("INSERT INTO Offset(id, offset) VALUES(?, ?)");
        verify(mockStatement).setString(1, "testId");
        verify(mockStatement).setLong(2, 123L);
        verify(mockStatement).executeUpdate();
        verify(mockStatement).close();
    }

    @Test
    void testInsertOffsetTableSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test SQLException"));

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            assertDoesNotThrow(() -> databaseQueryExecutor.insertOffsetTable("testId", 123L));

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseError(eq("SQL_EXCEPTION: "), exceptionCaptor.capture()));
            assertEquals("Test SQLException", exceptionCaptor.getValue().getMessage());
        }
    }

    @Test
    void testInsertInventoryTableSuccess() throws SQLException {
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        Map<String, Long> inventoryBatch = new HashMap<>();
        inventoryBatch.put("sku1", 10L);
        inventoryBatch.put("sku2", 20L);

        assertDoesNotThrow(() -> databaseQueryExecutor.insertInventoryTable(inventoryBatch));

        verify(mockConnection).prepareStatement("INSERT INTO Inventory(sku_id,quantity) VALUES(?, ?)");
        verify(mockStatement, times(inventoryBatch.size())).setString(anyInt(), anyString());
        verify(mockStatement, times(inventoryBatch.size())).setLong(anyInt(), anyLong());
        verify(mockStatement, times(inventoryBatch.size())).addBatch();
        verify(mockStatement).executeBatch();
        verify(mockStatement).close();
    }

    @Test
    void testInsertInventoryTableSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test SQLException"));

        Map<String, Long> inventoryBatch = new HashMap<>();
        inventoryBatch.put("sku1", 10L);
        inventoryBatch.put("sku2", 20L);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            assertDoesNotThrow(() -> databaseQueryExecutor.insertInventoryTable(inventoryBatch));

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseError(eq("SQL_EXCEPTION: "), exceptionCaptor.capture()));
            assertEquals("Test SQLException", exceptionCaptor.getValue().getMessage());
        }
    }

    @Test
    void testUpdateOffsetTableSuccess() throws SQLException {
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        String id = "testId";
        long offset = 100L;

        assertDoesNotThrow(() -> databaseQueryExecutor.updateOffsetTable(id, offset));

        verify(mockConnection).prepareStatement("UPDATE Offset SET offset = ? WHERE id = ?");
        verify(mockStatement).setLong(1, offset);
        verify(mockStatement).setString(2, id);
        verify(mockStatement).executeUpdate();
        verify(mockStatement).close();
    }

    @Test
    void testUpdateOffsetTableSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test SQLException"));

        String id = "testId";
        long offset = 100L;

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            assertDoesNotThrow(() -> databaseQueryExecutor.updateOffsetTable(id, offset));

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseError(eq("SQL_EXCEPTION: "), exceptionCaptor.capture()));
            assertEquals("Test SQLException", exceptionCaptor.getValue().getMessage());
        }
    }

    @Test
    void testUpdateInventoryTableSuccess() throws SQLException {
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        Map<String, Long> inventoryBatch = new HashMap<>();
        inventoryBatch.put("sku1", 100L);
        inventoryBatch.put("sku2", 200L);

        assertDoesNotThrow(() -> databaseQueryExecutor.updateInventoryTable(inventoryBatch));

        verify(mockConnection).prepareStatement("UPDATE Inventory SET quantity = ? WHERE sku_id = ?");
        verify(mockStatement, times(inventoryBatch.size())).setLong(anyInt(), anyLong());
        verify(mockStatement, times(inventoryBatch.size())).setString(anyInt(), anyString());
        verify(mockStatement, times(inventoryBatch.size())).addBatch();
        verify(mockStatement).executeBatch();
        verify(mockStatement).close();
    }

    @Test
    void testUpdateInventoryTableSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test SQLException"));

        Map<String, Long> inventoryBatch = new HashMap<>();
        inventoryBatch.put("sku1", 100L);
        inventoryBatch.put("sku2", 200L);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            assertDoesNotThrow(() -> databaseQueryExecutor.updateInventoryTable(inventoryBatch));

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseError(eq("SQL_EXCEPTION: "), exceptionCaptor.capture()));
            assertEquals("Test SQLException", exceptionCaptor.getValue().getMessage());
        }
    }

    @Test
    void testDeleteInventoryTableSuccess() throws SQLException {
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<String> skuList = Arrays.asList("sku1", "sku2");

        assertDoesNotThrow(() -> databaseQueryExecutor.deleteInventoryTable(skuList));

        verify(mockConnection).prepareStatement("DELETE FROM Inventory WHERE sku_id = ?");
        verify(mockStatement, times(skuList.size())).setString(anyInt(), anyString());
        verify(mockStatement, times(skuList.size())).addBatch();
        verify(mockStatement).executeBatch();
        verify(mockStatement).close();
    }

    @Test
    void testDeleteInventoryTableSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test SQLException"));

        List<String> skuList = Arrays.asList("sku1", "sku2");

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            assertDoesNotThrow(() -> databaseQueryExecutor.deleteInventoryTable(skuList));

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseError(eq("SQL_EXCEPTION: "), exceptionCaptor.capture()));
            assertEquals("Test SQLException", exceptionCaptor.getValue().getMessage());
        }
    }

    @Test
    void testGetMaxOffsetSuccess() throws SQLException {
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("offset")).thenReturn(123L);

        Long maxOffset = databaseQueryExecutor.getMaxOffset("test-id");

        assertEquals(123L, maxOffset);
        verify(mockConnection).prepareStatement("SELECT offset FROM Offset WHERE id = ?");
        verify(mockStatement).setString(1, "test-id");
        verify(mockStatement).executeQuery();
        verify(mockResultSet).next();
        verify(mockResultSet).getLong("offset");
    }

    @Test
    void testGetMaxOffsetNoResult() throws SQLException {
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            Long maxOffset = databaseQueryExecutor.getMaxOffset("test-id");

            assertNull(maxOffset);
            verify(mockConnection).prepareStatement("SELECT offset FROM Offset WHERE id = ?");
            verify(mockStatement).setString(1, "test-id");
            verify(mockStatement).executeQuery();
            verify(mockResultSet).next();

            ArgumentCaptor<StackTraceElement[]> stackTraceCaptor = ArgumentCaptor.forClass(StackTraceElement[].class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseInfo(eq("CANNOT_GET_MAX_OFFSET"), stackTraceCaptor.capture()));
        }
    }

    @Test
    void testGetMaxOffsetSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test SQLException"));

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            Long maxOffset = databaseQueryExecutor.getMaxOffset("test-id");

            assertNull(maxOffset);

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseError(eq("SQL_EXCEPTION: "), exceptionCaptor.capture()));
            assertEquals("Test SQLException", exceptionCaptor.getValue().getMessage());
        }
    }

    @Test
    void testGetSkuQuantitySuccess() throws SQLException {
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("quantity")).thenReturn(50L);

        Long quantity = databaseQueryExecutor.getSkuQuantity("sku-123");

        assertEquals(50L, quantity);
        verify(mockConnection).prepareStatement("SELECT quantity FROM Inventory WHERE sku_id = ?");
        verify(mockStatement).setString(1, "sku-123");
        verify(mockStatement).executeQuery();
        verify(mockResultSet).next();
        verify(mockResultSet).getLong("quantity");
    }

    @Test
    void testGetSkuQuantityNoResult() throws SQLException {
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            Long quantity = databaseQueryExecutor.getSkuQuantity("sku-123");

            assertNull(quantity);
            verify(mockConnection).prepareStatement("SELECT quantity FROM Inventory WHERE sku_id = ?");
            verify(mockStatement).setString(1, "sku-123");
            verify(mockStatement).executeQuery();
            verify(mockResultSet).next();

            ArgumentCaptor<StackTraceElement[]> stackTraceCaptor = ArgumentCaptor.forClass(StackTraceElement[].class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseInfo(eq("CANNOT_GET_SKU: sku-123"), stackTraceCaptor.capture()));
        }
    }

    @Test
    void testGetSkuQuantitySQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test SQLException"));

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            Long quantity = databaseQueryExecutor.getSkuQuantity("sku-123");

            assertNull(quantity);

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseError(eq("SQL_EXCEPTION: "), exceptionCaptor.capture()));
            assertEquals("Test SQLException", exceptionCaptor.getValue().getMessage());
        }
    }

    @Test
    void testGetInventoryRecordsSuccess() throws SQLException {
        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockResultSet.getString("sku_id")).thenReturn("sku-123").thenReturn("sku-456");
        when(mockResultSet.getLong("quantity")).thenReturn(50L).thenReturn(100L);

        Map<String, Long> inventoryRecords = databaseQueryExecutor.getInventoryRecords(2L);

        assertEquals(2, inventoryRecords.size());
        assertEquals(50L, inventoryRecords.get("sku-123"));
        assertEquals(100L, inventoryRecords.get("sku-456"));
        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery("SELECT sku_id, quantity FROM Inventory LIMIT 2");
        verify(mockResultSet, times(2)).getString("sku_id");
        verify(mockResultSet, times(2)).getLong("quantity");
        verify(mockResultSet, times(3)).next();
    }

    @Test
    void testGetInventoryRecordsNoResult() throws SQLException {
        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Map<String, Long> inventoryRecords = databaseQueryExecutor.getInventoryRecords(2L);

        assertTrue(inventoryRecords.isEmpty());
        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery("SELECT sku_id, quantity FROM Inventory LIMIT 2");
        verify(mockResultSet).next();
    }

    @Test
    void testGetInventoryRecordsSQLException() throws SQLException {
        when(mockConnection.createStatement()).thenThrow(new SQLException("Test SQLException"));

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            Map<String, Long> inventoryRecords = databaseQueryExecutor.getInventoryRecords(2L);

            assertTrue(inventoryRecords.isEmpty());

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseError(eq("SQL_EXCEPTION: "), exceptionCaptor.capture()));
            assertEquals("Test SQLException", exceptionCaptor.getValue().getMessage());
        }
    }

    @Test
    void testGetInventorySkuIdsSuccess() throws SQLException {
        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockResultSet.getString("sku_id")).thenReturn("sku-123").thenReturn("sku-456");

        List<String> inventorySkuIds = databaseQueryExecutor.getInventorySkuIds();

        assertEquals(2, inventorySkuIds.size());
        assertEquals("sku-123", inventorySkuIds.get(0));
        assertEquals("sku-456", inventorySkuIds.get(1));
        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery("SELECT sku_id FROM Inventory");
        verify(mockResultSet, times(2)).getString("sku_id");
        verify(mockResultSet, times(3)).next();
    }

    @Test
    void testGetInventorySkuIdsNoResult() throws SQLException {
        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<String> inventorySkuIds = databaseQueryExecutor.getInventorySkuIds();

        assertTrue(inventorySkuIds.isEmpty());
        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery("SELECT sku_id FROM Inventory");
        verify(mockResultSet).next();
    }

    @Test
    void testGetInventorySkuIdsSQLException() throws SQLException {
        when(mockConnection.createStatement()).thenThrow(new SQLException("Test SQLException"));

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            List<String> inventorySkuIds = databaseQueryExecutor.getInventorySkuIds();

            assertTrue(inventorySkuIds.isEmpty());

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseError(eq("SQL_EXCEPTION: "), exceptionCaptor.capture()));
            assertEquals("Test SQLException", exceptionCaptor.getValue().getMessage());
        }
    }

    @Test
    void testCloseSuccess() throws SQLException {
        databaseQueryExecutor.close();

        verify(mockConnection).close();
    }

    @Test
    void testCloseSQLException() throws SQLException {
        doThrow(new SQLException("Test SQLException")).when(mockConnection).close();

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            databaseQueryExecutor.close();

            ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
            mockedLogger.verify(() -> DatabaseLogger.logDatabaseError(eq("SQL_EXCEPTION: "), exceptionCaptor.capture()));
            assertEquals("Test SQLException", exceptionCaptor.getValue().getMessage());
        }
    }
}
