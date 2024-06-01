package processor.other.database.migration;

import processor.component.database.DatabaseConnector;
import processor.component.database.DatabaseConstants;
import processor.component.database.DatabaseQueryExecutor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseMigration {

    public static void migrate() {
        DatabaseConnector databaseConnector = DatabaseConnector.databaseConnectorFactory();

        // create processor.component.database
        Connection mySqlConnection = databaseConnector.mySqlConnect();
        DatabaseQueryExecutor MySqlQueryExecutor = new DatabaseQueryExecutor(mySqlConnection);
        MySqlQueryExecutor.dropAndCreateDatabase(DatabaseConstants.getDatabaseName());
        MySqlQueryExecutor.close();

        // create tables
        Connection databaseConnection = databaseConnector.databaseConnect();
        DatabaseQueryExecutor databaseQueryExecutor = new DatabaseQueryExecutor(databaseConnection);

        databaseQueryExecutor.createTable(CreateInventoryTable_194602052024.getStatement(), "Inventory");
        databaseQueryExecutor.createTable(CreateOffsetTable_193402052024.getStatement(), "Offset");
        databaseQueryExecutor.close();
    }

    public static void main(String[] args) throws IOException, SQLException {
        DatabaseMigration.migrate();
    }
}
