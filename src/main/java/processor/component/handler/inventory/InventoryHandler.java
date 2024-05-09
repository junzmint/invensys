package processor.component.handler.inventory;

import com.google.gson.Gson;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.framework.support.Message;
import lombok.Getter;
import utils.logging.LoggerUtil;

import java.util.HashMap;
import java.util.Map;

public class InventoryHandler {
    private static final String DEFERRED_ERROR = "HEADER_INFO_MISSING_ERR";
    private static final String MSG_BODY_ERROR = "JSON_FORMAT_ERR";
    private static final String PARSE_SUCCESS = "PARSE_SUCCESS";


    public InventoryHandler() {
    }

    // Get corrId
    private Long getCorrId(Message message) {
        return message.headers().getLong("corrId", null);
    }

    private String getReplyAdr(Message message) {
        return message.headers().getString("replyTo", null);
    }

    public void handle(Message message) {
        Long corrId = getCorrId(message);
        String replyTo = getReplyAdr(message);
        InventoryRequest inventoryRequest = objectMap(message);
        String res = null;

        if (corrId == null || replyTo == null) {
            res = DEFERRED_ERROR;
        } else if (inventoryRequest == null) {
            res = MSG_BODY_ERROR;
        } else {
            res = PARSE_SUCCESS;
        }

        System.out.println(res);
    }

    private InventoryRequest objectMap(Message message) {
        BObject header = message.headers();
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
            if (!("order".equals(type) || "create".equals(type) || "insert".equals(type) || "update".equals(type) || "delete".equals(type))) {
                return null;
            }

            // check orderId
            if ("order".equals(type)) {
                String orderId = inventoryRequest.getOrderId();
                if (orderId == null || orderId.isEmpty()) {
                    return null;
                }
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
        private String orderId;
        private HashMap<String, Long> skuList;
    }
}
