package utils.database.seeder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.database.migration.DatabaseMigration;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class Seeder {
    private static final Logger log = LoggerFactory.getLogger(Seeder.class);
    private static final Random random = new Random();
    private final String user;
    private final String password;
    private final String connector;
    private final String host;
    private final String port;
    private final String databaseName;

    public Seeder(String user, String password, String connector, String host, String port, String databaseName) {
        this.user = user;
        this.password = password;
        this.connector = connector;
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
    }

    public static void main(String[] args) throws IOException {
        Seeder.seed();
    }

    public static void seed() throws IOException {
        // Database config
        Properties appProps = new Properties();
        appProps.load(new FileInputStream("config/application.properties"));
        String connector = appProps.getProperty("DB_CONNECTION");
        String host = appProps.getProperty("DB_HOST");
        String port = appProps.getProperty("DB_PORT");
        String dbName = appProps.getProperty("DB_DATABASE");
        String user = appProps.getProperty("DB_USERNAME");
        String password = appProps.getProperty("DB_PASSWORD");

        // Create seeder
        Seeder seeder = new Seeder(user, password, connector, host, port, dbName);

        // Seeding data
        // Offset table
        String tableName = "Offset";
        List<String> columnNames = new ArrayList<>();
        columnNames.add("id");
        columnNames.add("offset");

        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{"MaxOffset", -1});

        String status = seeder.insertData(tableName, columnNames, data);
        log.info(status);

        // Inventory table
        tableName = "Inventory";
        columnNames = new ArrayList<>();
        columnNames.add("sku_id");
        columnNames.add("quantity");

        data = new ArrayList<>();
        for (int sku = 0; sku < 1000; sku++) {
            UUID uuid = UUID.randomUUID();
            long randomNumber = random.nextInt(100) + 1;
            data.add(new Object[]{uuid.toString(), randomNumber});
        }

        status = seeder.insertData(tableName, columnNames, data);
        log.info(status);
    }

    public String insertData(String tableName, List<String> columnNames, List<Object[]> data) {
        String dbUrl = "jdbc:" + this.connector + "://" + this.host + ":" + this.port;
        Connection conn = null;
        PreparedStatement pstmt = null;
        String result = null;

        try {
            // Connect to SQL server
            conn = DriverManager.getConnection(dbUrl, this.user, this.password);
            // Check whether database is existed
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet resultSet = metaData.getCatalogs();
            boolean databaseExists = false;
            while (resultSet.next()) {
                String dbName = resultSet.getString(1);
                if (dbName.equalsIgnoreCase(this.databaseName)) {
                    databaseExists = true;
                    break;
                }
            }
            resultSet.close();
            // If not, then create
            if (!databaseExists) {
                // Nếu cơ sở dữ liệu chưa tồn tại, chạy Migration.migrater
                DatabaseMigration.migrate();
            }
            conn.close();

            dbUrl = "jdbc:" + this.connector + "://" + this.host + ":" + this.port + "/" + this.databaseName;
            conn = DriverManager.getConnection(dbUrl, this.user, this.password);
            // Insert records
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

            // PreparedStatement instance create
            pstmt = conn.prepareStatement(sql.toString());

            // Set data
            for (Object[] rowData : data) {
                for (int i = 0; i < rowData.length; i++) {
                    pstmt.setObject(i + 1, rowData[i]);
                }
                pstmt.addBatch();
            }

            // Batch processing
            pstmt.executeBatch();

            result = "Data inserted successfully";
        } catch (SQLException se) {
            // JDBC Exception
            se.printStackTrace();
            result = "Failed to insert data: " + se.getMessage();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
                if (result == null) {
                    result = "Failed to close connection: " + se.getMessage();
                }
            }
            return result;
        }
    }
}

