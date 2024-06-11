package processor.component.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseQueryExecutor {
    private final Connection connection;

    public DatabaseQueryExecutor(Connection connection) {
        this.connection = connection;
    }

    // create database, drop database if it exists
    public void dropAndCreateDatabase(String dbName) {
        dropDatabase(dbName);
        createDatabase(dbName);
    }

    private void dropDatabase(String dbName) {
        String sql = "DROP DATABASE IF EXISTS " + dbName;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.executeUpdate();
            DatabaseLogger.logDatabaseInfo("DB_DROPPED", Thread.currentThread().getStackTrace());
        } catch (SQLException exception) {
            DatabaseLogger.logDatabaseError("SQL_EXCEPTION: ", exception);
        }
    }

    private void createDatabase(String dbName) {
        String sql = "CREATE DATABASE " + dbName;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.executeUpdate();
            DatabaseLogger.logDatabaseInfo("DB_CREATED", Thread.currentThread().getStackTrace());
        } catch (SQLException exception) {
            DatabaseLogger.logDatabaseError("SQL_EXCEPTION: ", exception);
        }
    }

    // create table if it not exists
    public void createTable(String queryStatement, String tableName) {
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate(queryStatement);
            DatabaseLogger.logDatabaseInfo("TABLE_CREATED: " + tableName, Thread.currentThread().getStackTrace());
        } catch (SQLException exception) {
            DatabaseLogger.logDatabaseError("SQL_EXCEPTION: ", exception);
        }
    }

    // insert Offset table
    public void insertOffsetTable(String id, Long offset) {
        String SQL = "INSERT INTO Offset(id, offset) VALUES(?, ?)";
        try (PreparedStatement statement = this.connection.prepareStatement(SQL)) {
            statement.setString(1, id);
            statement.setLong(2, offset);
            statement.executeUpdate();
            // DatabaseLogger.logDatabaseInfo("OFFSET_INSERTED", Thread.currentThread().getStackTrace());
        } catch (SQLException exception) {
            DatabaseLogger.logDatabaseError("SQL_EXCEPTION: ", exception);
        }
    }


    // insert Inventory table batch
    public void insertInventoryTable(Map<String, Long> inventoryBatch) {
        String SQL = "INSERT INTO Inventory(sku_id,quantity) " + "VALUES(?, ?)";
        try (PreparedStatement statement = this.connection.prepareStatement(SQL)) {
            for (Map.Entry<String, Long> entry : inventoryBatch.entrySet()) {
                statement.setString(1, entry.getKey());
                statement.setLong(2, entry.getValue());
                statement.addBatch();
            }
            statement.executeBatch();
            // DatabaseLogger.logDatabaseInfo("INVENTORY_BATCH_INSERTED", Thread.currentThread().getStackTrace());
        } catch (SQLException exception) {
            DatabaseLogger.logDatabaseError("SQL_EXCEPTION: ", exception);
        }
    }

    // update Offset table
    public void updateOffsetTable(String id, Long offset) {
        String SQL = "UPDATE Offset SET offset = ? WHERE id = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(SQL)) {
            statement.setLong(1, offset);
            statement.setString(2, id);
            statement.executeUpdate();
            // DatabaseLogger.logDatabaseInfo("OFFSET_UPDATED", Thread.currentThread().getStackTrace());
        } catch (SQLException exception) {
            DatabaseLogger.logDatabaseError("SQL_EXCEPTION: ", exception);
        }
    }


    // update Inventory table batch
    public void updateInventoryTable(Map<String, Long> inventoryBatch) {
        String SQL = "UPDATE Inventory SET quantity = ? WHERE sku_id = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(SQL)) {
            for (Map.Entry<String, Long> entry : inventoryBatch.entrySet()) {
                statement.setLong(1, entry.getValue());
                statement.setString(2, entry.getKey());
                statement.addBatch();
            }
            statement.executeBatch();
            // DatabaseLogger.logDatabaseInfo("INVENTORY_BATCH_UPDATED", Thread.currentThread().getStackTrace());
        } catch (SQLException exception) {
            DatabaseLogger.logDatabaseError("SQL_EXCEPTION: ", exception);
        }
    }

    // delete Inventory table batch
    public void deleteInventoryTable(List<String> skuList) {
        String SQL = "DELETE FROM Inventory WHERE sku_id = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(SQL)) {
            for (String skuId : skuList) {
                statement.setString(1, skuId);
                statement.addBatch();
            }
            statement.executeBatch();
            // DatabaseLogger.logDatabaseInfo("INVENTORY_BATCH_DELETED", Thread.currentThread().getStackTrace());
        } catch (SQLException exception) {
            DatabaseLogger.logDatabaseError("SQL_EXCEPTION: ", exception);
        }
    }

    // get MaxOffset
    public Long getMaxOffset(String id) {
        String SQL = "SELECT offset FROM Offset WHERE id = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(SQL)) {
            statement.setString(1, id);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                return result.getLong("offset");
            }
        } catch (SQLException exception) {
            DatabaseLogger.logDatabaseError("SQL_EXCEPTION: ", exception);
        }
        DatabaseLogger.logDatabaseInfo("CANNOT_GET_MAX_OFFSET", Thread.currentThread().getStackTrace());
        return null;
    }


    // get quantity of a sku
    public Long getSkuQuantity(String skuId) {
        String SQL = "SELECT quantity FROM Inventory WHERE sku_id = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(SQL)) {
            statement.setString(1, skuId);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                return result.getLong("quantity");
            }
        } catch (SQLException exception) {
            DatabaseLogger.logDatabaseError("SQL_EXCEPTION: ", exception);
        }
        DatabaseLogger.logDatabaseInfo("CANNOT_GET_SKU: " + skuId, Thread.currentThread().getStackTrace());
        return null;
    }


    // get quantity of skus to init load cache
    public Map<String, Long> getInventoryRecords(Long numberOfRecords) {
        Map<String, Long> inventoryRecords = new HashMap<>();
        String SQL = "SELECT sku_id, quantity FROM Inventory LIMIT " + numberOfRecords;
        try (Statement statement = this.connection.createStatement()) {
            ResultSet result = statement.executeQuery(SQL);
            while (result.next()) {
                String skuId = result.getString("sku_id");
                long quantity = result.getLong("quantity");
                inventoryRecords.put(skuId, quantity);
            }
        } catch (SQLException exception) {
            DatabaseLogger.logDatabaseError("SQL_EXCEPTION: ", exception);
        }
        return inventoryRecords;
    }

    // get skuId from inventory to generate CSV data test
    public List<String> getInventorySkuIds() {
        List<String> inventorySkuIds = new ArrayList<>();
        String SQL = "SELECT sku_id FROM Inventory";
        try (Statement statement = this.connection.createStatement()) {
            ResultSet result = statement.executeQuery(SQL);
            while (result.next()) {
                String skuId = result.getString("sku_id");
                inventorySkuIds.add(skuId);
            }
        } catch (SQLException exception) {
            DatabaseLogger.logDatabaseError("SQL_EXCEPTION: ", exception);
        }
        return inventorySkuIds;
    }

    // Close connection
    public void close() {
        try {
            this.connection.close();
        } catch (SQLException exception) {
            DatabaseLogger.logDatabaseError("SQL_EXCEPTION: ", exception);
        }
    }
}
