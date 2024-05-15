package processor.component.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import database.DatabaseQueryExecutor;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

@Getter
public class LocalCache {
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

    public CacheStats getStats() {
        return this.cache.stats();
    }

    public void printStats() {
        CacheStats stats = this.cache.stats();
        System.out.println("Hit count: " + stats.hitCount());
        System.out.println("Hit rate: " + stats.hitRate());
        System.out.println("Miss count: " + stats.missCount());
        System.out.println("Miss rate: " + stats.missRate());
        System.out.println("Load success count: " + stats.loadSuccessCount());
        System.out.println("Load exception count: " + stats.loadExceptionCount());
        System.out.println("Total load time (nanoseconds): " + stats.totalLoadTime());
        System.out.println("Eviction count: " + stats.evictionCount());
    }

    public void onStop() {
        this.cache.cleanUp();
    }
}
