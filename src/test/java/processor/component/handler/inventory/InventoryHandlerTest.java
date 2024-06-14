package processor.component.handler.inventory;

import com.google.gson.Gson;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.framework.support.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import processor.component.cache.LocalCache;
import processor.component.disruptor.producer.BatchEventProducer;
import processor.component.disruptor.producer.MessageEventProducer;
import processor.component.handler.HandlerLogger;
import processor.component.handler.inventory.InventoryHandler.InventoryRequest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class InventoryHandlerTest {

    @Mock
    private LocalCache mockLocalCache;

    @Mock
    private MessageEventProducer mockMessageEventProducer;

    @Mock
    private BatchEventProducer mockBatchEventProducer;

    private InventoryHandler inventoryHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        inventoryHandler = new InventoryHandler(100L, mockMessageEventProducer, mockBatchEventProducer);
        injectMockLocalCache(inventoryHandler, mockLocalCache);
    }

    private void injectMockLocalCache(InventoryHandler handler, LocalCache localCache) {
        try {
            Field localCacheField = InventoryHandler.class.getDeclaredField("localCache");
            localCacheField.setAccessible(true);
            localCacheField.set(handler, localCache);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testStart() {
        inventoryHandler.start(100L, 10L);
        verify(mockLocalCache, times(1)).initCache(100L);
        verify(mockLocalCache, times(1)).scheduleCacheLogging(10L, TimeUnit.SECONDS);
    }

    @Test
    void testClose() {
        inventoryHandler.close();
        verify(mockLocalCache, times(1)).stop();
    }

    @Test
    void testHandleInsert() {
        HashMap<String, Long> skuList = new HashMap<>();
        skuList.put("sku1", 100L);
        InventoryHandler.InventoryRequest request = new InventoryHandler.InventoryRequest();
        request.setType("insert");
        request.setSkuList(skuList);

        Message message = buildMessage(request, "corrId", "replyTo");
        inventoryHandler.handle(message, 1L, false);

        verify(mockLocalCache, times(1)).putAll(skuList);
        verify(mockBatchEventProducer, times(1)).onData("insert", 1L, skuList);
        verify(mockMessageEventProducer, times(1)).onData(eq("corrId"), eq("replyTo"), eq("INSERTED"));
    }

    @Test
    void testHandleUpdate() {
        HashMap<String, Long> skuList = new HashMap<>();
        skuList.put("sku1", 100L);
        InventoryHandler.InventoryRequest request = new InventoryHandler.InventoryRequest();
        request.setType("update");
        request.setSkuList(skuList);

        Message message = buildMessage(request, "corrId", "replyTo");
        inventoryHandler.handle(message, 1L, false);

        verify(mockLocalCache, times(1)).putAll(skuList);
        verify(mockBatchEventProducer, times(1)).onData("update", 1L, skuList);
        verify(mockMessageEventProducer, times(1)).onData(eq("corrId"), eq("replyTo"), eq("UPDATED"));
    }

    @Test
    void testHandleDelete() {
        HashMap<String, Long> skuList = new HashMap<>();
        skuList.put("sku1", 0L);
        InventoryHandler.InventoryRequest request = new InventoryHandler.InventoryRequest();
        request.setType("delete");
        request.setSkuList(skuList);

        Message message = buildMessage(request, "corrId", "replyTo");
        inventoryHandler.handle(message, 1L, false);

        verify(mockLocalCache, times(1)).deleteAll(skuList.keySet());
        verify(mockBatchEventProducer, times(1)).onData("delete", 1L, skuList);
        verify(mockMessageEventProducer, times(1)).onData(eq("corrId"), eq("replyTo"), eq("DELETED"));
    }

    @Test
    void testHandleOrder() {
        // Test for valid order
        HashMap<String, Long> skuList = new HashMap<>();
        skuList.put("sku1", 10L);
        when(mockLocalCache.get("sku1")).thenReturn(100L);

        InventoryHandler.InventoryRequest request = new InventoryHandler.InventoryRequest();
        request.setType("order");
        request.setSkuList(skuList);

        Message message = buildMessage(request, "corrId", "replyTo");
        inventoryHandler.handle(message, 1L, false);

        verify(mockLocalCache, times(1)).get("sku1");
        verify(mockLocalCache, times(1)).putAll(anyMap());
        verify(mockMessageEventProducer, times(1)).onData(eq("corrId"), eq("replyTo"), eq("RESERVED"));
        verify(mockBatchEventProducer, never()).onData(any(), anyLong(), any());

        // Test for invalid SKU
        when(mockLocalCache.get("sku1")).thenReturn((long) -1);

        inventoryHandler.handle(message, 1L, false);
        verify(mockMessageEventProducer, times(1)).onData(eq("corrId"), eq("replyTo"), eq("INVALID_SKU: sku1"));

        // Test for not enough quantity
        when(mockLocalCache.get("sku1")).thenReturn(5L);

        inventoryHandler.handle(message, 1L, false);
        verify(mockMessageEventProducer, times(1)).onData(eq("corrId"), eq("replyTo"), eq("NOT ENOUGH: sku1"));
    }

    @Test
    void testHandleRollback() {
        HashMap<String, Long> skuList = new HashMap<>();
        skuList.put("sku1", 10L);
        when(mockLocalCache.get("sku1")).thenReturn(100L);

        InventoryHandler.InventoryRequest request = new InventoryHandler.InventoryRequest();
        request.setType("rollback");
        request.setSkuList(skuList);

        Message message = buildMessage(request, "corrId", "replyTo");
        inventoryHandler.handle(message, 1L, false);

        verify(mockLocalCache, times(1)).get("sku1");
        verify(mockLocalCache, times(1)).put(anyString(), anyLong());
        verify(mockMessageEventProducer, times(1)).onData(eq("corrId"), eq("replyTo"), eq("ROLLBACK"));
    }

    @Test
    void testHandleEndOfBatch() {
        HashMap<String, Long> skuList = new HashMap<>();
        skuList.put("sku1", 10L);
        when(mockLocalCache.get("sku1")).thenReturn(100L);

        InventoryHandler.InventoryRequest request = new InventoryHandler.InventoryRequest();
        request.setType("order");
        request.setSkuList(skuList);

        Message message = buildMessage(request, "corrId", "replyTo");
        inventoryHandler.handle(message, 1L, true);

        verify(mockLocalCache, times(1)).get("sku1");
        verify(mockLocalCache, times(1)).putAll(anyMap());
        verify(mockMessageEventProducer, times(1)).onData(eq("corrId"), eq("replyTo"), eq("RESERVED"));
        verify(mockBatchEventProducer, times(1)).onData(eq("order"), eq(1L), anyMap());
        verify(mockBatchEventProducer, times(1)).onData(eq("order"), anyLong(), anyMap());
    }

    @Test
    void testHandleDeferredError() {
        InventoryHandler.InventoryRequest request = new InventoryHandler.InventoryRequest();
        Message message = buildMessage(request, "", "replyTo");
        inventoryHandler.handle(message, 1L, false);
        verify(mockMessageEventProducer, times(1)).onData(eq(""), eq("replyTo"), eq("DEFERRED_ERROR"));

        message = buildMessage(request, "corrId", "");
        inventoryHandler.handle(message, 1L, false);
        verify(mockMessageEventProducer, times(1)).onData(eq("corrId"), eq(""), eq("DEFERRED_ERROR"));
    }

    @Test
    void testHandleMsgBodyError() {
        Message message = buildMessage(null, "corrId", "replyTo");
        inventoryHandler.handle(message, 1L, false);
        verify(mockMessageEventProducer, times(1)).onData(eq("corrId"), eq("replyTo"), eq("MSG_BODY_ERROR"));
    }

    private Message buildMessage(InventoryHandler.InventoryRequest request, String corrId, String replyTo) {
        Gson gson = new Gson();
        String json = gson.toJson(request);
        Message message = Message.ofAny(BElement.ofJson(json));
        message.headers().setAny("corrId", corrId);
        message.headers().setAny("replyTo", replyTo);
        return message;
    }

    @Test
    void testObjectMapBodyNull() {
        Message message = mock(Message.class);
        when(message.body()).thenReturn(null);

        InventoryRequest result = invokeObjectMap(message);

        assertNull(result);
    }

    @Test
    void testObjectMapParseException() {
        Message message = mock(Message.class);
        when(message.body()).thenReturn(BElement.ofAny("invalid_json"));

        try (MockedStatic<HandlerLogger> ignored = mockStatic(HandlerLogger.class)) {
            InventoryRequest result = invokeObjectMap(message);

            assertNull(result);
        }
    }

    @Test
    void testObjectMapInvalidType() {
        InventoryRequest request = new InventoryRequest();
        request.setType("invalid_type");
        request.setSkuList(new HashMap<>());

        Message message = createMockMessageFromRequest(request);

        InventoryRequest result = invokeObjectMap(message);

        assertNull(result);
    }

    @Test
    void testObjectMapSkuListNull() {
        InventoryRequest request = new InventoryRequest();
        request.setType("order");
        request.setSkuList(null);

        Message message = createMockMessageFromRequest(request);

        InventoryRequest result = invokeObjectMap(message);

        assertNull(result);
    }

    @Test
    void testObjectMapSkuListEmpty() {
        InventoryRequest request = new InventoryRequest();
        request.setType("order");
        request.setSkuList(new HashMap<>());

        Message message = createMockMessageFromRequest(request);

        InventoryRequest result = invokeObjectMap(message);

        assertNull(result);
    }

    @Test
    void testObjectMapSkuListMaxValue() {
        InventoryRequest request = new InventoryRequest();
        request.setType("order");
        HashMap<String, Long> skuList = new HashMap<>();
        skuList.put("sku1", Long.MAX_VALUE);
        request.setSkuList(skuList);

        Message message = createMockMessageFromRequest(request);

        InventoryRequest result = invokeObjectMap(message);

        assertNull(result);
    }

    @Test
    void testObjectMapSkuListNegativeQuantity() {
        InventoryRequest request = new InventoryRequest();
        request.setType("order");
        HashMap<String, Long> skuList = new HashMap<>();
        skuList.put("sku1", -1L);
        request.setSkuList(skuList);

        Message message = createMockMessageFromRequest(request);

        InventoryRequest result = invokeObjectMap(message);

        assertNull(result);
    }

    @Test
    void testObjectMapDeleteNonZeroQuantity() {
        InventoryRequest request = new InventoryRequest();
        request.setType("delete");
        HashMap<String, Long> skuList = new HashMap<>();
        skuList.put("sku1", 1L);
        request.setSkuList(skuList);

        Message message = createMockMessageFromRequest(request);

        InventoryRequest result = invokeObjectMap(message);

        assertNull(result);
    }

    @Test
    void testObjectMapValidRequest() {
        InventoryRequest request = new InventoryRequest();
        request.setType("order");
        HashMap<String, Long> skuList = new HashMap<>();
        skuList.put("sku1", 10L);
        request.setSkuList(skuList);

        Message message = createMockMessageFromRequest(request);

        InventoryHandler.InventoryRequest result = invokeObjectMap(message);

        assertNotNull(result);
        assertEquals("order", result.getType());
        assertEquals(1, result.getSkuList().size());
        assertEquals(10L, result.getSkuList().get("sku1"));
    }

    private InventoryRequest invokeObjectMap(Message message) {
        try {
            Method method = InventoryHandler.class.getDeclaredMethod("objectMap", Message.class);
            method.setAccessible(true);
            return (InventoryRequest) method.invoke(inventoryHandler, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Message createMockMessageFromRequest(InventoryRequest request) {
        Message message = mock(Message.class);

        BObject body = BObject.of("type", request.getType());
        body.setAny("skuList", request.getSkuList());
        when(message.body()).thenReturn(body);
        return message;
    }
}
