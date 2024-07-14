package processor.component.handler.batch;

import database.DatabaseConnector;
import database.DatabaseQueryExecutor;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BatchHandler {
    private final Map<String, Long> batchEventBatch;
    private DatabaseQueryExecutor databaseQueryExecutor;

    public BatchHandler() {
        DatabaseConnector databaseConnector = DatabaseConnector.databaseConnectorFactory();
        Connection databaseConnection = databaseConnector.databaseConnect();
        this.databaseQueryExecutor = new DatabaseQueryExecutor(databaseConnection);

        this.batchEventBatch = new HashMap<>();
    }

    public void handle(String type, Long offset, Map<String, Long> batch, Boolean endOfBatch) {
        switch (type) {
            case "order":
//                this.databaseQueryExecutor.updateInventoryTable(batch);
                this.batchEventBatch.putAll(batch);

                if (endOfBatch) {
                    this.databaseQueryExecutor.updateOffsetTable("MaxOffset", offset);
                    this.databaseQueryExecutor.updateInventoryTable(batchEventBatch);
                    this.batchEventBatch.clear();
                }
                break;
            case "insert":
                this.databaseQueryExecutor.insertInventoryTable(batch);
                this.databaseQueryExecutor.updateOffsetTable("MaxOffset", offset);
                break;
            case "update":
                this.databaseQueryExecutor.updateInventoryTable(batch);
                this.databaseQueryExecutor.updateOffsetTable("MaxOffset", offset);
                break;
            case "delete":
                this.databaseQueryExecutor.deleteInventoryTable(new ArrayList<>(batch.keySet()));
                this.databaseQueryExecutor.updateOffsetTable("MaxOffset", offset);
                break;
            default:
                break;
        }
    }

    public void close() {
        this.databaseQueryExecutor = null;
        this.batchEventBatch.clear();
    }
}