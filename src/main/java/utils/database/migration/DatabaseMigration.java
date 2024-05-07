package utils.database.migration;

import database.DatabaseConnector;
import database.DatabaseConstants;
import database.DatabaseQueryExecutor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseMigration {

    public static void migrate() throws IOException, SQLException {
        DatabaseConnector databaseConnector = new DatabaseConnector(
                DatabaseConstants.getDatabaseConnnection(),
                DatabaseConstants.getDatabaseHost(),
                DatabaseConstants.getDatabasePort(),
                DatabaseConstants.getDatabaseUser(),
                DatabaseConstants.getDatabasePassword(),
                DatabaseConstants.getDatabaseName()
        );

        // Create database
        Connection mySqlConnection = databaseConnector.mySqlConnect();
        DatabaseQueryExecutor MySqlQueryExecutor = new DatabaseQueryExecutor(mySqlConnection);
        MySqlQueryExecutor.dropAndCreateDatabase(DatabaseConstants.getDatabaseName());
        MySqlQueryExecutor.close();

        // Create tables
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
