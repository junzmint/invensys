package database.seeder;

import database.DatabaseConnector;
import database.DatabaseLogger;
import database.DatabaseQueryExecutor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

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
        } catch (NumberFormatException e) {
            DatabaseLogger.logDatabaseError("SEED_ERROR", e);
        }
    }

    public static void seed(Long numberOfRecords) {
        DatabaseConnector databaseConnector = DatabaseConnector.databaseConnectorFactory();

        Connection databaseConnection = databaseConnector.databaseConnect();
        DatabaseQueryExecutor databaseQueryExecutor = new DatabaseQueryExecutor(databaseConnection);

        seedInventoryTable(databaseQueryExecutor, numberOfRecords);
        seedOffsetTable(databaseQueryExecutor);
        databaseQueryExecutor.close();
    }

    public static void seedInventoryTable(DatabaseQueryExecutor databaseQueryExecutor, long numberOfRecords) {
        Map<String, Long> inventoryBatch = new HashMap<>();
        for (long sku = 0; sku < numberOfRecords; sku++) {
            UUID uuid = UUID.randomUUID();
            String skuId = uuid.toString().substring(0, 4);
            long quantity = random.nextInt(1000) + 1;
            inventoryBatch.put(skuId, quantity);
        }

        databaseQueryExecutor.insertInventoryTable(inventoryBatch);
        DatabaseLogger.logDatabaseInfo("INVENTORY_SEEDED", Thread.currentThread().getStackTrace());
    }

    public static void seedOffsetTable(DatabaseQueryExecutor databaseQueryExecutor) {
        databaseQueryExecutor.insertOffsetTable("MaxOffset", (long) -1);

        DatabaseLogger.logDatabaseInfo("OFFSET_SEEDED", Thread.currentThread().getStackTrace());
    }
}
