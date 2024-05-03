package utils.database.migration;

import persistence.database.DatabaseConfigLoader;
import persistence.database.DatabaseConnector;
import persistence.database.MySqlConnector;
import utils.database.migration.creator.database.DatabaseCreator;
import utils.database.migration.creator.table.TableCreator;
import utils.database.migration.migration.CreateInventoryTable_194602052024;
import utils.database.migration.migration.CreateOffsetTable_193402052024;
import utils.logging.LoggerUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseMigration {

    public static void migrate() throws IOException, SQLException {
        // Database config
        Properties dbProps = DatabaseConfigLoader.loadDatabaseConfig("config/application.properties");
        String connector = dbProps.getProperty("DB_CONNECTION");
        String host = dbProps.getProperty("DB_HOST");
        String port = dbProps.getProperty("DB_PORT");
        String dbName = dbProps.getProperty("DB_DATABASE");
        String user = dbProps.getProperty("DB_USERNAME");
        String password = dbProps.getProperty("DB_PASSWORD");

        // Create database
        MySqlConnector mySqlConnector = new MySqlConnector(connector, host, port, user, password);
        DatabaseCreator dbCreator = new DatabaseCreator(mySqlConnector.getConnector());
        String status = dbCreator.createDatabase(dbName);
        LoggerUtil.logInfo(status);

        // Create tables
        List<String> createTableList = new ArrayList<>();
        createTableList.add(CreateOffsetTable_193402052024.getStatement());
        createTableList.add(CreateInventoryTable_194602052024.getStatement());

        DatabaseConnector databaseConnector = new DatabaseConnector(connector, host, port, user, password, dbName);
        TableCreator tableCreator = new TableCreator(databaseConnector.getConnector());

        status = tableCreator.createTables(createTableList);
        LoggerUtil.logInfo(status);
    }

    public static void main(String[] args) throws IOException, SQLException {
        DatabaseMigration.migrate();
    }
}
