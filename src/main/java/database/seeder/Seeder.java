package database.seeder;

import database.DatabaseConnector;
import database.DatabaseQueryExecutor;
import logging.LoggerUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Seeder {
    private static final Random random = new Random();

    public static void main(String[] args) throws IOException, SQLException {
        if (args.length < 1) {
            LoggerUtil.logError("MISSING_NUMBER_OF_RECORDS_PARAM");
        }
        try {
            Long numberOfRecords = Long.parseLong(args[0]);
            System.out.println("Seeding");
            Seeder.seed(numberOfRecords);
        } catch (NumberFormatException e) {
            LoggerUtil.logError(e.getMessage());
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
    }

    public static void seedOffsetTable(DatabaseQueryExecutor databaseQueryExecutor) {
        databaseQueryExecutor.insertOffsetTable("MaxOffset", (long) -1);
    }
}
