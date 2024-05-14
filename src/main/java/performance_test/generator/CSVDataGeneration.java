package performance_test.generator;

import com.google.gson.Gson;
import database.DatabaseConnector;
import database.DatabaseQueryExecutor;
import logging.LoggerUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CSVDataGeneration {
    private final DatabaseQueryExecutor databaseQueryExecutor;

    private CSVDataGeneration() {
        this.databaseQueryExecutor = new DatabaseQueryExecutor(DatabaseConnector.databaseConnectorFactory().databaseConnect());
    }

    public static void main(String[] args) {
        CSVDataGeneration csvDataGeneration = new CSVDataGeneration();
        csvDataGeneration.generate();
    }

    public void generate() {
        String path = "src/main/java/utils/testing/csv/file/json_data.csv";
        List<String> skuIds = this.getSkuIds();
        List<Map<String, Long>> data = this.createDataSet(skuIds);
        this.csvGenerate(data, path);
    }

    private List<String> getSkuIds() {
        return this.databaseQueryExecutor.getInventorySkuIds();
    }

    private List<Map<String, Long>> createDataSet(List<String> skuIds) {
        Random random = new Random();
        List<Map<String, Long>> data = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            // get 10 random skuIds
            Collections.shuffle(skuIds);
            List<String> skuIdsTen = skuIds.subList(0, 10);
            // create skuList
            Map<String, Long> skuList = new HashMap<>();
            for (String skuId : skuIdsTen) {
                skuList.put(skuId, random.nextLong(10));
            }
            // put data to the data map
            data.add(skuList);
        }
        return data;
    }

    private void csvGenerate(List<Map<String, Long>> data, String path) {
        InventoryRequest inventoryRequest = new InventoryRequest();
        inventoryRequest.setType("order");

        try {
            FileWriter writer = new FileWriter(path);

            writer.append("JSON_DATA");
            writer.append("\n");
            for (Map<String, Long> skuList : data) {
                inventoryRequest.setSkuList(skuList);
                // object -> JSON
                Gson gson = new Gson();
                String json = gson.toJson(inventoryRequest);

                // write to file
                writer.append(json);
                writer.append("\n");
            }

            writer.flush();
            writer.close();

            System.out.println("Success write to CSV");
        } catch (IOException e) {
            LoggerUtil.logError(e.getMessage());
        }
    }

    @Getter
    @Setter
    private static class InventoryRequest {
        private String type;
        private Map<String, Long> skuList;
    }
}
