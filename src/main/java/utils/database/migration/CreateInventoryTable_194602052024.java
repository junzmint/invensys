package utils.database.migration;

public class CreateInventoryTable_194602052024 {
    private static final String stmt = "CREATE TABLE IF NOT EXISTS Inventory (" +
            "sku_id VARCHAR(255) PRIMARY KEY," +
            "quantity BIGINT" +
            ")";

    public static String getStatement() {
        return stmt;
    }
}
