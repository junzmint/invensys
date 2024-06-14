package processor.component.handler.inventory;

import com.google.gson.Gson;
import io.gridgo.bean.BElement;
import io.gridgo.framework.support.Message;
import lombok.Getter;
import lombok.Setter;
import processor.component.cache.LocalCache;
import processor.component.disruptor.producer.BatchEventProducer;
import processor.component.disruptor.producer.MessageEventProducer;
import processor.component.handler.HandlerLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class InventoryHandler {
    private static final String DEFERRED_ERROR = "DEFERRED_ERROR";
    private static final String MSG_BODY_ERROR = "MSG_BODY_ERROR";

    private final LocalCache localCache;

    // this map is using for update sku quantity after serving order in a batch
    // if endOfBatch then batch produce then clear
    // we need to check endOfBatch on every message we handle
    private final Map<String, Long> inventoryOrderBatch;

    private final MessageEventProducer messageEventProducer;

    private final BatchEventProducer batchEventProducer;

    public InventoryHandler(Long cacheSize, MessageEventProducer messageEventProducer, BatchEventProducer batchEventProducer) {
        this.localCache = new LocalCache(cacheSize);
        this.inventoryOrderBatch = new HashMap<>();
        this.messageEventProducer = messageEventProducer;
        this.batchEventProducer = batchEventProducer;
    }

    public void start(Long cacheInitRecords, Long cacheStatLogAfter) {
        // init cache load records
        this.localCache.initCache(cacheInitRecords);
        // set up cache statistics logging
        this.localCache.scheduleCacheLogging(cacheStatLogAfter, TimeUnit.SECONDS);
    }

    public void close() {
        this.inventoryOrderBatch.clear();
        this.localCache.stop();
    }

    private void insert(Long offset, InventoryRequest inventoryRequest, String corrId, String replyTo) {
        // update local cache
        this.localCache.putAll(inventoryRequest.getSkuList());
        // batch produce and message produce
        this.batchEventProducer.onData("insert", offset, inventoryRequest.getSkuList());
        this.messageEventProducer.onData(corrId, replyTo, "INSERTED");
    }

    private void update(Long offset, InventoryRequest inventoryRequest, String corrId, String replyTo) {
        // update local cache
        this.localCache.putAll(inventoryRequest.getSkuList());
        // batch produce and message produce
        this.batchEventProducer.onData("update", offset, inventoryRequest.getSkuList());
        this.messageEventProducer.onData(corrId, replyTo, "UPDATED");
    }

    private void delete(Long offset, InventoryRequest inventoryRequest, String corrId, String replyTo) {
        // update local cache
        this.localCache.deleteAll(inventoryRequest.getSkuList().keySet());
        // batch produce and message produce
        this.batchEventProducer.onData("delete", offset, inventoryRequest.getSkuList());
        this.messageEventProducer.onData(corrId, replyTo, "DELETED");
    }

    private void order(InventoryRequest inventoryRequest, String corrId, String replyTo) {
        Map<String, Long> skuList = inventoryRequest.getSkuList();
        Map<String, Long> inventoryOrder = new HashMap<>();
        // Check quantity
        for (Map.Entry<String, Long> entry : skuList.entrySet()) {
            String skuId = entry.getKey();
            Long inventoryQuantity = this.localCache.get(skuId);
            if (inventoryQuantity == (long) -1) {
                // key not existed message produce
                this.messageEventProducer.onData(corrId, replyTo, "INVALID_SKU: " + skuId);
                return;
            }
            inventoryQuantity = inventoryQuantity - entry.getValue();
            if (inventoryQuantity < 0) {
                // Not enough quantity message produce
                this.messageEventProducer.onData(corrId, replyTo, "NOT ENOUGH: " + skuId);
                return;
            }
            inventoryOrder.put(skuId, inventoryQuantity);
        }

        // update local cache
        this.localCache.putAll(inventoryOrder);
        // message produce after serving an order
        this.messageEventProducer.onData(corrId, replyTo, "RESERVED");
        // update batching map after serving an order
        this.inventoryOrderBatch.putAll(inventoryOrder);
    }

    private void rollback(InventoryRequest inventoryRequest, String corrId, String replyTo) {
        Map<String, Long> skuList = inventoryRequest.getSkuList();

        // rollback quantity
        for (Map.Entry<String, Long> entry : skuList.entrySet()) {
            String skuId = entry.getKey();
            long inventoryQuantityRollBack = entry.getValue();
            long inventoryQuantity = this.localCache.get(skuId) + inventoryQuantityRollBack;
            // rollback cache
            this.localCache.put(skuId, inventoryQuantity);
            // update batch
            this.inventoryOrderBatch.put(skuId, inventoryQuantity);
        }

        // message
        this.messageEventProducer.onData(corrId, replyTo, "ROLLBACK");
    }

    public void handle(Message message, Long offset, Boolean endOfBatch) {
        String corrId = getCorrId(message);
        String replyTo = getReplyAdr(message);
        InventoryRequest inventoryRequest = objectMap(message);

        if (corrId.isEmpty() || replyTo.isEmpty()) {
            // reply produce
            this.messageEventProducer.onData(corrId, replyTo, DEFERRED_ERROR);
            return;
        } else if (inventoryRequest == null) {
            // reply produce
            this.messageEventProducer.onData(corrId, replyTo, MSG_BODY_ERROR);
            return;
        }
        switch (inventoryRequest.type) {
            case "order":
                this.order(inventoryRequest, corrId, replyTo);
                break;
            case "insert":
                this.insert(offset, inventoryRequest, corrId, replyTo);
                break;
            case "update":
                this.update(offset, inventoryRequest, corrId, replyTo);
                break;
            case "delete":
                this.delete(offset, inventoryRequest, corrId, replyTo);
                break;
            case "rollback":
                this.rollback(inventoryRequest, corrId, replyTo);
                break;
            default:
                break;
        }

        // if endOfBatch then batch produce then clear
        if (endOfBatch) {
            // batch produce
            this.batchEventProducer.onData("order", offset, inventoryOrderBatch);
            // clear batch for a new batch
            this.inventoryOrderBatch.clear();
        }
    }

    // get corrId
    private String getCorrId(Message message) {
        return message.headers().getString("corrId", null).replaceAll("[^\\x00-\\x7F]", "");
    }

    // get message reply address
    private String getReplyAdr(Message message) {
        return message.headers().getString("replyTo", null).replaceAll("[^\\x00-\\x7F]", "");
    }

    // unmarshall
    private InventoryRequest objectMap(Message message) {
        BElement body = message.body();

        if (body == null) {
            return null;
        }

        Gson gson = new Gson();
        // get message content
        try {
            InventoryRequest inventoryRequest = gson.fromJson(body.toJson(), InventoryRequest.class);

            // check type
            String type = inventoryRequest.getType();
            if (!("order".equals(type) || "insert".equals(type) || "update".equals(type) || "delete".equals(type) || "rollback".equals(type))) {
                return null;
            }

            // check skuList
            Map<String, Long> skuList = inventoryRequest.getSkuList();

            if (skuList == null || skuList.isEmpty()) {
                return null;
            }

            for (Map.Entry<String, Long> entry : skuList.entrySet()) {
                Long quantity = skuList.get(entry.getKey());
                if (quantity == Long.MAX_VALUE) {
                    return null;
                }
                if (quantity < 0) {
                    return null;
                }
                if ("delete".equals(type) && quantity != 0) {
                    return null;
                }
            }

            return inventoryRequest;
        } catch (Exception exception) {
            HandlerLogger.logProcessorError("PARSE_OBJECT_ERROR", exception);
            return null;
        }
    }

    @Getter
    @Setter
    static class InventoryRequest {
        private String type;
        private HashMap<String, Long> skuList;
    }
}
