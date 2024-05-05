package utils.database.seeder;

import database.DatabaseConfigLoader;
import database.DatabaseConnector;
import utils.logging.LoggerUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class Seeder {
    private static final Random random = new Random();

    public static void main(String[] args) throws IOException, SQLException {
        Seeder.seed();
    }

    public static void seed() throws IOException, SQLException {
        Properties dbProps = DatabaseConfigLoader.loadDatabaseConfig("config/application.properties");

        String connector = dbProps.getProperty("DB_CONNECTION");
        String host = dbProps.getProperty("DB_HOST");
        String port = dbProps.getProperty("DB_PORT");
        String dbName = dbProps.getProperty("DB_DATABASE");
        String user = dbProps.getProperty("DB_USERNAME");
        String password = dbProps.getProperty("DB_PASSWORD");

        DatabaseConnector databaseConnector = new DatabaseConnector(connector, host, port, user, password, dbName);

        seedOffsetTable(databaseConnector.getConnector());
        seedInventoryTable(databaseConnector.getConnector());

        databaseConnector.close();
    }

    public static void seedOffsetTable(Connection connector) {
        List<String> columnNames = Arrays.asList("id", "offset");
        List<Object[]> data = Collections.singletonList(new Object[]{"MaxOffset", -1});

        String status = insertData(connector, "Offset", columnNames, data);
        LoggerUtil.logInfo(status);
    }

    public static void seedInventoryTable(Connection connector) {
        List<String> columnNames = Arrays.asList("sku_id", "quantity");
        List<Object[]> data = new ArrayList<>();
        for (int sku = 0; sku < 1000; sku++) {
            UUID uuid = UUID.randomUUID();
            String skuId = uuid.toString().substring(0, 4);
            long randomNumber = random.nextInt(100) + 1;
            data.add(new Object[]{skuId, randomNumber});
        }

        String status = insertData(connector, "Inventory", columnNames, data);
        LoggerUtil.logInfo(status);
    }

    public static String insertData(Connection connector, String tableName, List<String> columnNames, List<Object[]> data) {
        try {
            PreparedStatement pstmt = connector.prepareStatement(generateInsertQuery(tableName, columnNames));
            for (Object[] rowData : data) {
                for (int i = 0; i < rowData.length; i++) {
                    pstmt.setObject(i + 1, rowData[i]);
                }
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            return "Data inserted successfully";
        } catch (SQLException se) {
            LoggerUtil.logError("Failed to insert data into " + tableName + ": " + se.getMessage());
            return "Failed to insert data: " + se.getMessage();
        }
    }

    private static String generateInsertQuery(String tableName, List<String> columnNames) {
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        for (String columnName : columnNames) {
            sql.append(columnName).append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(") VALUES (");
        for (int i = 0; i < columnNames.size(); i++) {
            sql.append("?,");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        return sql.toString();
    }
}
