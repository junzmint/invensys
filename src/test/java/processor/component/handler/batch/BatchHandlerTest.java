package processor.component.handler.batch;

class BatchHandlerTest {

//    @Mock
//    private DatabaseConnector mockDatabaseConnector;
//
//    @Mock
//    private Connection mockConnection;
//
//    @Mock
//    private DatabaseQueryExecutor mockDatabaseQueryExecutor;
//
//    private BatchHandler batchHandler;
//
//    @BeforeEach
//    void setUp() throws Exception {
//        MockitoAnnotations.openMocks(this);
//
//        // Mocking DatabaseConnector and its static factory method
//        when(mockDatabaseConnector.databaseConnect()).thenReturn(mockConnection);
//        try (MockedStatic<DatabaseConnector> mockedStatic = mockStatic(DatabaseConnector.class)) {
//            mockedStatic.when(DatabaseConnector::databaseConnectorFactory).thenReturn(mockDatabaseConnector);
//
//            // Creating BatchHandler instance and injecting mocked DatabaseQueryExecutor
//            batchHandler = new BatchHandler();
//            setDatabaseQueryExecutor(batchHandler, mockDatabaseQueryExecutor);
//        }
//    }
//
//    @Test
//    void testConstructor() {
//        assertNotNull(batchHandler);
//    }
//
//    @Test
//    void testHandleOrderType() {
//        // Test handling "order" type batch
//        Map<String, Long> batch = new HashMap<>();
//        batch.put("item1", 10L);
//        batch.put("item2", 20L);
//
//        batchHandler.handle("order", 100L, batch, true);
//
//        // Verify that updateInventoryTable and updateOffsetTable are called with correct parameters
//        verify(mockDatabaseQueryExecutor, times(1)).updateInventoryTable(batch);
//        verify(mockDatabaseQueryExecutor, times(1)).updateOffsetTable("MaxOffset", 100L);
//    }
//
//    @Test
//    void testHandleInsertType() {
//        // Test handling "insert" type batch
//        Map<String, Long> batch = new HashMap<>();
//        batch.put("item1", 10L);
//
//        batchHandler.handle("insert", 200L, batch, false);
//
//        // Verify that insertInventoryTable and updateOffsetTable are called with correct parameters
//        verify(mockDatabaseQueryExecutor, times(1)).insertInventoryTable(batch);
//        verify(mockDatabaseQueryExecutor, times(1)).updateOffsetTable("MaxOffset", 200L);
//    }
//
//    @Test
//    void testHandleUpdateType() {
//        // Test handling "update" type batch
//        Map<String, Long> batch = new HashMap<>();
//        batch.put("item1", 15L);
//
//        batchHandler.handle("update", 300L, batch, false);
//
//        // Verify that updateInventoryTable and updateOffsetTable are called with correct parameters
//        verify(mockDatabaseQueryExecutor, times(1)).updateInventoryTable(batch);
//        verify(mockDatabaseQueryExecutor, times(1)).updateOffsetTable("MaxOffset", 300L);
//    }
//
//    @Test
//    void testHandleDeleteType() {
//        // Test handling "delete" type batch
//        Map<String, Long> batch = new HashMap<>();
//        batch.put("item1", 0L);
//
//        batchHandler.handle("delete", 400L, batch, false);
//
//        // Verify that deleteInventoryTable and updateOffsetTable are called with correct parameters
//        verify(mockDatabaseQueryExecutor, times(1)).deleteInventoryTable(new ArrayList<>(batch.keySet()));
//        verify(mockDatabaseQueryExecutor, times(1)).updateOffsetTable("MaxOffset", 400L);
//    }
//
//    @Test
//    void testHandleEndOfBatch() {
//        // Test handling "order" type batch at end of batch
//        Map<String, Long> batch = new HashMap<>();
//        batch.put("item1", 10L);
//
//        batchHandler.handle("order", 500L, batch, true);
//
//        // Verify that updateInventoryTable and updateOffsetTable are called with correct parameters
//        verify(mockDatabaseQueryExecutor, times(1)).updateInventoryTable(batch);
//        verify(mockDatabaseQueryExecutor, times(1)).updateOffsetTable("MaxOffset", 500L);
//    }
//
//    @Test
//    void testClose() {
//        // Test closing BatchHandler
//        batchHandler.close();
//
//        // Verify that no methods are called on DatabaseQueryExecutor after closing
//        verify(mockDatabaseQueryExecutor, times(0)).updateInventoryTable(anyMap());
//    }
//
//    // Utility method to set private field databaseQueryExecutor using reflection
//    private void setDatabaseQueryExecutor(BatchHandler handler, DatabaseQueryExecutor databaseQueryExecutor) throws Exception {
//        Field field = BatchHandler.class.getDeclaredField("databaseQueryExecutor");
//        field.setAccessible(true);
//        field.set(handler, databaseQueryExecutor);
//    }
}
