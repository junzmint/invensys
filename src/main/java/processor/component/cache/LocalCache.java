package processor.component.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import database.DatabaseQueryExecutor;
import lombok.Getter;
import processor.component.ProcessorLogger;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public class LocalCache {
    private final ScheduledExecutorService logScheduler = Executors.newScheduledThreadPool(1);
    private final DatabaseQueryExecutor databaseQueryExecutor;
    private final LoadingCache<String, Long> cache;

    public LocalCache(DatabaseQueryExecutor databaseQueryExecutor, Long size) {
        this.databaseQueryExecutor = databaseQueryExecutor;

        CacheLoader<String, Long> loader = new CacheLoader<>() {
            @Override
            @Nonnull
            public Long load(@Nonnull String key) throws Exception {
                return loadWhenCacheMiss(key);
            }
        };
        this.cache = CacheBuilder.newBuilder().recordStats().maximumSize(size).build(loader);
    }

    private Long loadWhenCacheMiss(String key) {
        Long value = this.databaseQueryExecutor.getSkuQuantity(key);
        // If return -1 mean this sku is not existed
        return Objects.requireNonNullElseGet(value, () -> (long) -1);
    }

    public Long get(String key) {
        Long value = this.cache.getUnchecked(key);
        if (value == -1) {
            this.delete(key);
            return (long) -1;
        }
        return value;
    }

    public void put(String key, Long value) {
        this.cache.put(key, value);
    }

    public void putAll(Map<String, Long> records) {
        this.cache.putAll(records);
    }

    public void delete(String key) {
        this.cache.invalidate(key);
    }

    public void deleteAll(Iterable<String> keys) {
        this.cache.invalidateAll(keys);
    }

    public void initCache(Long numberOfRecords) {
        Map<String, Long> inventoryRecords = this.databaseQueryExecutor.getInventoryRecords(numberOfRecords);
        this.putAll(inventoryRecords);
    }

    private String getStats() {
        CacheStats stats = this.cache.stats();

        return String.format(
                "Cache statistics: Total requests=%d, Hit count=%d, Miss count=%d, Eviction count=%d, Hit rate=%.2f%%, Miss rate=%.2f%%, Total entries=%d, Total load time=%d nanoseconds",
                stats.requestCount(),
                stats.hitCount(),
                stats.missCount(),
                stats.evictionCount(),
                stats.hitRate() * 100,
                stats.missRate() * 100,
                cache.size(),
                stats.totalLoadTime());
    }

    private void printStats() {
        System.out.println(this.getStats());
    }

    public void scheduleCacheLogging(long interval, TimeUnit timeUnit) {
        this.logScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                ProcessorLogger.logCacheStat(getStats());
            }
        }, 0, interval, timeUnit);
    }

    private void shutdownLogScheduler() {
        this.logScheduler.shutdown();
    }

    public void stop() {
        this.printStats();
        this.cache.cleanUp();
        this.shutdownLogScheduler();
    }
}
