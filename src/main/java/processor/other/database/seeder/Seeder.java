package processor.other.database.seeder;

import processor.component.database.DatabaseConnector;
import processor.component.database.DatabaseLogger;
import processor.component.database.DatabaseQueryExecutor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Seeder {
    private static final Random random = new Random();

    public static void main(String[] args) throws IOException, SQLException {
        if (args.length < 1) {
            DatabaseLogger.logDatabaseError("MISSING_NUMBER_OF_RECORDS_PARAM", new MissingFormatArgumentException("NUMBER_OF_RECORDS"));
        }
        try {
            Long numberOfRecords = Long.parseLong(args[0]);
            System.out.println("Seeding");
            Seeder.seed(numberOfRecords);
        } catch (NumberFormatException exception) {
            DatabaseLogger.logDatabaseError("SEED_ERROR", exception);
        }
    }

    public static void seed(Long numberOfRecords) {
        seedInventoryTable(numberOfRecords);
        seedOffsetTable();
    }

    public static void seedInventoryTable(long numberOfRecords) {
        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Callable<Void>> tasks = new ArrayList<>();

        long recordsPerThread = numberOfRecords / numThreads;

        for (int i = 0; i < numThreads; i++) {
            long start = i * recordsPerThread;
            long end = (i == numThreads - 1) ? numberOfRecords : (i + 1) * recordsPerThread;
            tasks.add(() -> {
                DatabaseConnector databaseConnector = DatabaseConnector.databaseConnectorFactory();
                Connection databaseConnection = databaseConnector.databaseConnect();
                DatabaseQueryExecutor databaseQueryExecutor = new DatabaseQueryExecutor(databaseConnection);

                Map<String, Long> inventoryBatch = new HashMap<>();
                for (long sku = start; sku < end; sku++) {
                    UUID uuid = UUID.randomUUID();
                    String skuId = uuid.toString();
                    long quantity = random.nextInt(2000) + 1;
                    inventoryBatch.put(skuId, quantity);
                }
                databaseQueryExecutor.insertInventoryTable(inventoryBatch);

                databaseQueryExecutor.close();

                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        DatabaseLogger.logDatabaseInfo("INVENTORY_SEEDED", Thread.currentThread().getStackTrace());
    }

    public static void seedOffsetTable() {
        DatabaseConnector databaseConnector = DatabaseConnector.databaseConnectorFactory();
        Connection databaseConnection = databaseConnector.databaseConnect();
        DatabaseQueryExecutor databaseQueryExecutor = new DatabaseQueryExecutor(databaseConnection);

        databaseQueryExecutor.insertOffsetTable("MaxOffset", (long) -1);

        DatabaseLogger.logDatabaseInfo("OFFSET_SEEDED", Thread.currentThread().getStackTrace());

        databaseQueryExecutor.close();
    }
}
