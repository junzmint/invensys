package utils.database.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.database.migration.migrations.CreateInventoryTable_194602052024;
import utils.database.migration.migrations.CreateOffsetTable_193402052024;
import utils.database.migration.query.DatabaseInitializer;
import utils.database.migration.query.TableCreator;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Migration {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    public static void migration() throws IOException {
        // Database config
        Properties appProps = new Properties();
        appProps.load(new FileInputStream("config/application.properties"));
        String connector = appProps.getProperty("DB_CONNECTION");
        String host = appProps.getProperty("DB_HOST");
        String port = appProps.getProperty("DB_PORT");
        String dbName = appProps.getProperty("DB_DATABASE");
        String user = appProps.getProperty("DB_USERNAME");
        String password = appProps.getProperty("DB_PASSWORD");

        // Create database
        DatabaseInitializer initializer = new DatabaseInitializer(user, password, connector, host, port);
        String status = initializer.create(dbName);
        logger.info(status);

        // Query list
        List<String> createTableList = new ArrayList<>();
        createTableList.add(CreateOffsetTable_193402052024.getStatement());
        createTableList.add(CreateInventoryTable_194602052024.getStatement());

        // Create tables
        TableCreator creator = new TableCreator(user, password, connector, host, port, dbName);

        status = creator.create(createTableList);
        logger.info(status);
    }

    public static void main(String[] args) throws IOException {
        Migration.migration();
    }
}
