package processor.other.database.migration;

public class CreateOffsetTable_193402052024 {
    private static final String stmt = "CREATE TABLE IF NOT EXISTS Offset (" +
            "id VARCHAR(255) PRIMARY KEY," +
            "offset BIGINT" +
            ")";

    public static String getStatement() {
        return stmt;
    }
}
