package processor.component.handler.batch;

import database.DatabaseConnector;
import database.DatabaseQueryExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BatchHandlerTest {

    @Mock
    private DatabaseConnector mockDatabaseConnector;

    @Mock
    private Connection mockConnection;

    @Mock
    private DatabaseQueryExecutor mockDatabaseQueryExecutor;

    private BatchHandler batchHandler;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(mockDatabaseConnector.databaseConnect()).thenReturn(mockConnection);
        try (MockedStatic<DatabaseConnector> mockedStatic = mockStatic(DatabaseConnector.class)) {
            mockedStatic.when(DatabaseConnector::databaseConnectorFactory).thenReturn(mockDatabaseConnector);
            batchHandler = new BatchHandler();
            setDatabaseQueryExecutor(batchHandler, mockDatabaseQueryExecutor);
        }
    }

    @Test
    void testConstructor() {
        assertNotNull(batchHandler);
    }

    @Test
    void testHandleOrder() {
        Map<String, Long> batch = new HashMap<>();
        batch.put("sku1", 100L);

        batchHandler.handle("order", 1L, batch);

        verify(mockDatabaseQueryExecutor, times(1)).updateInventoryTable(batch);
        verify(mockDatabaseQueryExecutor, times(1)).updateOffsetTable("MaxOffset", 1L);
    }

    @Test
    void testHandleInsert() {
        Map<String, Long> batch = new HashMap<>();
        batch.put("sku1", 100L);

        batchHandler.handle("insert", 1L, batch);

        verify(mockDatabaseQueryExecutor, times(1)).insertInventoryTable(batch);
        verify(mockDatabaseQueryExecutor, times(1)).updateOffsetTable("MaxOffset", 1L);
    }

    @Test
    void testHandleUpdate() {
        Map<String, Long> batch = new HashMap<>();
        batch.put("sku1", 100L);

        batchHandler.handle("update", 1L, batch);

        verify(mockDatabaseQueryExecutor, times(1)).updateInventoryTable(batch);
        verify(mockDatabaseQueryExecutor, times(1)).updateOffsetTable("MaxOffset", 1L);
    }

    @Test
    void testHandleDelete() {
        Map<String, Long> batch = new HashMap<>();
        batch.put("sku1", 100L);

        batchHandler.handle("delete", 1L, batch);

        verify(mockDatabaseQueryExecutor, times(1)).deleteInventoryTable(new ArrayList<>(batch.keySet()));
        verify(mockDatabaseQueryExecutor, times(1)).updateOffsetTable("MaxOffset", 1L);
    }

    @Test
    void testHandleUnknownType() {
        Map<String, Long> batch = new HashMap<>();
        batch.put("sku1", 100L);

        batchHandler.handle("unknown", 1L, batch);

        verify(mockDatabaseQueryExecutor, never()).updateInventoryTable(any());
        verify(mockDatabaseQueryExecutor, never()).updateOffsetTable(any(), anyLong());
        verify(mockDatabaseQueryExecutor, never()).insertInventoryTable(any());
        verify(mockDatabaseQueryExecutor, never()).deleteInventoryTable(any());
    }

    @Test
    void testClose() {
        batchHandler.close();
        assertNull(getDatabaseQueryExecutor(batchHandler));
    }

    private void setDatabaseQueryExecutor(BatchHandler handler, DatabaseQueryExecutor databaseQueryExecutor) throws Exception {
        Field field = BatchHandler.class.getDeclaredField("databaseQueryExecutor");
        field.setAccessible(true);
        field.set(handler, databaseQueryExecutor);
    }

    private DatabaseQueryExecutor getDatabaseQueryExecutor(BatchHandler handler) {
        try {
            Field field = BatchHandler.class.getDeclaredField("databaseQueryExecutor");
            field.setAccessible(true);
            return (DatabaseQueryExecutor) field.get(handler);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
