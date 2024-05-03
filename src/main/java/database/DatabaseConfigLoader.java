package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConfigLoader {
    public static Properties loadDatabaseConfig(String configFilePath) throws IOException {
        Properties dbProps = new Properties();
        dbProps.load(new FileInputStream(configFilePath));
        return dbProps;
    }
}