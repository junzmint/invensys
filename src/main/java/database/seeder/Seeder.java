package database.seeder;

import database.DatabaseConnector;
import database.DatabaseQueryExecutor;

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
        Seeder.seed();
    }

    public static void seed() {
        DatabaseConnector databaseConnector = DatabaseConnector.databaseConnectorFactory();

        Connection databaseConnection = databaseConnector.databaseConnect();
        DatabaseQueryExecutor databaseQueryExecutor = new DatabaseQueryExecutor(databaseConnection);

        seedInventoryTable(databaseQueryExecutor, 10000);
        seedOffsetTable(databaseQueryExecutor);
        databaseQueryExecutor.close();
    }

    public static void seedInventoryTable(DatabaseQueryExecutor databaseQueryExecutor, long numberOfRecords) {
        Map<String, Long> inventoryBatch = new HashMap<>();
        for (long sku = 0; sku < numberOfRecords; sku++) {
            UUID uuid = UUID.randomUUID();
            String skuId = uuid.toString().substring(0, 4);
            long quantity = random.nextInt(10000) + 1;
            inventoryBatch.put(skuId, quantity);
        }

        databaseQueryExecutor.insertInventoryTable(inventoryBatch);
    }

    public static void seedOffsetTable(DatabaseQueryExecutor databaseQueryExecutor) {
        databaseQueryExecutor.insertOffsetTable("MaxOffset", (long) -1);
    }
}
