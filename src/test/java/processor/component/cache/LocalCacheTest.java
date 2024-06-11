package processor.component.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Marker;
import processor.component.database.DatabaseQueryExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class LocalCacheTest {

    private DatabaseQueryExecutor mockDatabaseQueryExecutor;
    private LocalCache cache;
    private ScheduledExecutorService mockScheduler;

    @BeforeEach
    void setUp() throws Exception {
        mockDatabaseQueryExecutor = mock(DatabaseQueryExecutor.class);
        mockScheduler = mock(ScheduledExecutorService.class);

        cache = new LocalCache(100L);

        // Inject the mocks using reflection
        var fieldDatabaseQueryExecutor = LocalCache.class.getDeclaredField("databaseQueryExecutor");
        fieldDatabaseQueryExecutor.setAccessible(true);
        fieldDatabaseQueryExecutor.set(cache, mockDatabaseQueryExecutor);

        var fieldLogScheduler = LocalCache.class.getDeclaredField("logScheduler");
        fieldLogScheduler.setAccessible(true);
        fieldLogScheduler.set(cache, mockScheduler);

        // Mock the database connection and executor
        when(mockDatabaseQueryExecutor.getSkuQuantity(anyString())).thenReturn(10L);
        when(mockDatabaseQueryExecutor.getInventoryRecords(anyLong())).thenReturn(new HashMap<>());
    }

    @Test
    void testLoadWhenCacheMiss() {
        when(mockDatabaseQueryExecutor.getSkuQuantity(anyString())).thenReturn(10L);
        Long value = cache.get("key");
        assertEquals(10L, value);

        when(mockDatabaseQueryExecutor.getSkuQuantity(anyString())).thenReturn(null);
        value = cache.get("missingKey");
        assertEquals(-1L, value);
    }

    @Test
    void testGet() {
        cache.put("key", 10L);
        assertEquals(10L, cache.get("key"));

        cache.put("missingKey", -1L);
        assertEquals(-1L, cache.get("missingKey"));
        assertNull(cache.getCache().getIfPresent("missingKey"));
    }

    @Test
    void testPut() {
        cache.put("key", 10L);
        assertEquals(10L, cache.getCache().getIfPresent("key"));
    }

    @Test
    void testPutAll() {
        Map<String, Long> records = new HashMap<>();
        records.put("key1", 10L);
        records.put("key2", 20L);
        cache.putAll(records);
        assertEquals(10L, cache.getCache().getIfPresent("key1"));
        assertEquals(20L, cache.getCache().getIfPresent("key2"));
    }

    @Test
    void testDelete() {
        cache.put("key", 10L);
        cache.delete("key");
        assertNull(cache.getCache().getIfPresent("key"));
    }

    @Test
    void testDeleteAll() {
        cache.put("key1", 10L);
        cache.put("key2", 20L);
        cache.deleteAll(java.util.Arrays.asList("key1", "key2"));
        assertNull(cache.getCache().getIfPresent("key1"));
        assertNull(cache.getCache().getIfPresent("key2"));
    }

    @Test
    void testInitCache() {
        Map<String, Long> records = new HashMap<>();
        records.put("key1", 10L);
        records.put("key2", 20L);
        when(mockDatabaseQueryExecutor.getInventoryRecords(2L)).thenReturn(records);

        cache.initCache(2L);
        assertEquals(10L, cache.getCache().getIfPresent("key1"));
        assertEquals(20L, cache.getCache().getIfPresent("key2"));
    }

    @Test
    void testScheduleCacheLogging() {
        cache.scheduleCacheLogging(10, TimeUnit.SECONDS);
        verify(mockScheduler).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(10L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testStop() {
        cache.put("key", 10L);
        cache.stop();

        verify(mockDatabaseQueryExecutor).close();
        verify(mockScheduler).shutdown();
    }

    @Test
    void testDatabaseQueryExecutorIsUsed() {
        cache.get("key");
        verify(mockDatabaseQueryExecutor).getSkuQuantity("key");

        cache.initCache(2L);
        verify(mockDatabaseQueryExecutor).getInventoryRecords(2L);
    }

    @Test
    void testSchedulerIsUsed() {
        cache.scheduleCacheLogging(10, TimeUnit.SECONDS);
        verify(mockScheduler).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(10L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testDatabaseQueryExecutorFieldCoverage() {
        assertEquals(mockDatabaseQueryExecutor, cache.getDatabaseQueryExecutor());
    }

    @Test
    void testLogSchedulerFieldCoverage() {
        assertEquals(mockScheduler, cache.getLogScheduler());
    }

    @Test
    void testLogCacheStat() throws Exception {
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        cache.scheduleCacheLogging(10, TimeUnit.SECONDS);
        verify(mockScheduler).scheduleAtFixedRate(runnableCaptor.capture(), eq(0L), eq(10L), eq(TimeUnit.SECONDS));

        Runnable scheduledTask = runnableCaptor.getValue();

        // Use reflection to replace the logger in LocalCacheLogger with a mock
        var loggerField = LocalCacheLogger.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        var mockLogger = mock(org.slf4j.Logger.class);
        loggerField.set(null, mockLogger);

        scheduledTask.run();

        verify(mockLogger).info(any(Marker.class), anyString());
    }
}
