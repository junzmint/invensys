package processor.component.handler.inventory;

import com.google.gson.Gson;
import io.gridgo.bean.BElement;
import io.gridgo.framework.support.Message;
import lombok.Getter;
import processor.component.cache.LocalCache;
import processor.component.disruptor.producer.BatchEventProducer;
import processor.component.disruptor.producer.MessageEventProducer;
import utils.logging.LoggerUtil;

import java.util.HashMap;
import java.util.Map;

public class InventoryHandler {
    private static final String DEFERRED_ERROR = "DEFERRED_ERROR";
    private static final String MSG_BODY_ERROR = "MSG_BODY_ERROR";

    private final LocalCache localCache;

    private final Map<String, Long> inventoryOrderBatch;

    private final MessageEventProducer messageEventProducer;

    private final BatchEventProducer batchEventProducer;

    public InventoryHandler(LocalCache localCache, MessageEventProducer messageEventProducer, BatchEventProducer batchEventProducer) {
        this.localCache = localCache;
        this.inventoryOrderBatch = new HashMap<>();
        this.messageEventProducer = messageEventProducer;
        this.batchEventProducer = batchEventProducer;
    }

    public void close() {
        // nothing to here
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

    private void order(Long offset, InventoryRequest inventoryRequest, String corrId, String replyTo, Boolean endOfBatch) {
        Map<String, Long> skuList = inventoryRequest.getSkuList();
        Map<String, Long> inventoryOrder = new HashMap<>();
        // Check quantity
        for (Map.Entry<String, Long> entry : skuList.entrySet()) {
            String skuId = entry.getKey();
            Long inventoryQuantity = this.localCache.get(skuId);
            if (inventoryQuantity == (long) -1) {
                // key not existed message produce
                this.messageEventProducer.onData(corrId, replyTo, "INVALID_SKU");
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
        // message produce after serving a order
        this.messageEventProducer.onData(corrId, replyTo, "RESERVED");
        // update batching map after serving a order
        this.inventoryOrderBatch.putAll(inventoryOrder);

        if (endOfBatch) {
            // batch produce
            this.batchEventProducer.onData("order", offset, inventoryOrderBatch);
            // clear batch for a new batch
            this.inventoryOrderBatch.clear();
        }
    }

    public void handle(Message message, Long offset, Boolean endOfBatch) {
        String corrId = getCorrId(message);
        String replyTo = getReplyAdr(message);
        InventoryRequest inventoryRequest = objectMap(message);

        if (corrId == null || replyTo == null) {
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
                this.order(offset, inventoryRequest, corrId, replyTo, endOfBatch);
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
            default:
                break;
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

    // get request content
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
            if (!("order".equals(type) || "insert".equals(type) || "update".equals(type) || "delete".equals(type))) {
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
        } catch (Exception e) {
            LoggerUtil.logError(e.getMessage());
            return null;
        }
    }

    @Getter
    private static class InventoryRequest {
        private String type;
        private HashMap<String, Long> skuList;
    }
}
