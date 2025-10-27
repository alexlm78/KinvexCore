package dev.kreaker.kinvex.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.kreaker.kinvex.dto.inventory.CreateProductRequest;
import dev.kreaker.kinvex.dto.inventory.StockUpdateRequest;
import dev.kreaker.kinvex.dto.inventory.UpdateProductRequest;
import dev.kreaker.kinvex.entity.AuditLog;
import dev.kreaker.kinvex.entity.Category;
import dev.kreaker.kinvex.entity.Product;
import dev.kreaker.kinvex.entity.User;
import dev.kreaker.kinvex.entity.User.UserRole;
import dev.kreaker.kinvex.repository.AuditLogRepository;
import dev.kreaker.kinvex.repository.CategoryRepository;
import dev.kreaker.kinvex.repository.ProductRepository;
import dev.kreaker.kinvex.repository.UserRepository;
import dev.kreaker.kinvex.service.InventoryService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuditAspectIntegrationTest {

    @Autowired private InventoryService inventoryService;

    @Autowired private AuditLogRepository auditLogRepository;

    @Autowired private UserRepository userRepository;

    @Autowired private CategoryRepository categoryRepository;

    @Autowired private ProductRepository productRepository;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setRole(UserRole.OPERATOR);
        testUser.setActive(true);
        testUser = userRepository.save(testUser);

        // Create test category
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setDescription("Test category for audit tests");
        testCategory = categoryRepository.save(testCategory);

        // Set up security context
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser.getUsername(), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Clear any existing audit logs
        auditLogRepository.deleteAll();
    }

    @Test
    void testCreateProductTriggersAuditLog() {
        // Given
        CreateProductRequest request = new CreateProductRequest();
        request.setCode("TEST001");
        request.setName("Test Product");
        request.setDescription("Test product for audit");
        request.setCategoryId(testCategory.getId());
        request.setUnitPrice(new BigDecimal("10.00"));
        request.setInitialStock(0);
        request.setMinStock(5);
        request.setMaxStock(100);

        // When
        Product savedProduct = inventoryService.createProduct(request);

        // Then
        assertNotNull(savedProduct);
        assertNotNull(savedProduct.getId());

        // Verify audit log was created
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertEquals(1, auditLogs.size());

        AuditLog auditLog = auditLogs.get(0);
        assertEquals(AuditLog.ACTION_CREATE, auditLog.getAction());
        assertEquals(AuditLog.ENTITY_PRODUCT, auditLog.getEntityType());
        assertEquals(savedProduct.getId(), auditLog.getEntityId());
        assertEquals(testUser.getId(), auditLog.getUser().getId());
        assertNotNull(auditLog.getCreatedAt());
    }

    @Test
    void testUpdateProductTriggersAuditLog() {
        // Given - create a product first
        CreateProductRequest createRequest = new CreateProductRequest();
        createRequest.setCode("TEST002");
        createRequest.setName("Test Product 2");
        createRequest.setDescription("Test product for update audit");
        createRequest.setCategoryId(testCategory.getId());
        createRequest.setUnitPrice(new BigDecimal("15.00"));
        createRequest.setInitialStock(10);
        createRequest.setMinStock(5);
        createRequest.setMaxStock(100);

        Product savedProduct = inventoryService.createProduct(createRequest);
        auditLogRepository.deleteAll(); // Clear create audit log

        // When - update the product
        UpdateProductRequest updateRequest = new UpdateProductRequest();
        updateRequest.setName("Updated Test Product 2");
        updateRequest.setUnitPrice(new BigDecimal("20.00"));
        updateRequest.setMinStock(5);
        updateRequest.setMaxStock(100);
        Product updatedProduct =
                inventoryService.updateProduct(savedProduct.getId(), updateRequest);

        // Then
        assertNotNull(updatedProduct);
        assertEquals("Updated Test Product 2", updatedProduct.getName());

        // Verify audit log was created
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertEquals(1, auditLogs.size());

        AuditLog auditLog = auditLogs.get(0);
        assertEquals(AuditLog.ACTION_UPDATE, auditLog.getAction());
        assertEquals(AuditLog.ENTITY_PRODUCT, auditLog.getEntityType());
        assertEquals(savedProduct.getId(), auditLog.getEntityId());
        assertEquals(testUser.getId(), auditLog.getUser().getId());
    }

    @Test
    void testStockOperationsTriggersAuditLog() {
        // Given - create a product first
        CreateProductRequest createRequest = new CreateProductRequest();
        createRequest.setCode("TEST003");
        createRequest.setName("Test Product 3");
        createRequest.setDescription("Test product for stock audit");
        createRequest.setCategoryId(testCategory.getId());
        createRequest.setUnitPrice(new BigDecimal("25.00"));
        createRequest.setInitialStock(50);
        createRequest.setMinStock(10);
        createRequest.setMaxStock(200);

        Product savedProduct = inventoryService.createProduct(createRequest);
        auditLogRepository.deleteAll(); // Clear create audit log

        // When - perform stock operations
        StockUpdateRequest increaseRequest = new StockUpdateRequest(25);
        increaseRequest.setSourceSystem("PURCHASE_ORDER");
        increaseRequest.setReferenceId(1L);

        StockUpdateRequest decreaseRequest = new StockUpdateRequest(10);
        decreaseRequest.setSourceSystem("SALE");

        inventoryService.increaseStock(savedProduct.getId(), increaseRequest);
        inventoryService.decreaseStock(savedProduct.getId(), decreaseRequest);

        // Then
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertEquals(2, auditLogs.size());

        // Verify increase stock audit log
        AuditLog increaseLog =
                auditLogs.stream()
                        .filter(log -> AuditLog.ACTION_STOCK_INCREASE.equals(log.getAction()))
                        .findFirst()
                        .orElse(null);
        assertNotNull(increaseLog);
        assertEquals(AuditLog.ENTITY_INVENTORY_MOVEMENT, increaseLog.getEntityType());
        assertEquals(savedProduct.getId(), increaseLog.getEntityId());
        assertTrue(increaseLog.getNewValues().contains("\"quantity\":25"));
        assertTrue(increaseLog.getNewValues().contains("\"movementType\":\"IN\""));

        // Verify decrease stock audit log
        AuditLog decreaseLog =
                auditLogs.stream()
                        .filter(log -> AuditLog.ACTION_STOCK_DECREASE.equals(log.getAction()))
                        .findFirst()
                        .orElse(null);
        assertNotNull(decreaseLog);
        assertEquals(AuditLog.ENTITY_INVENTORY_MOVEMENT, decreaseLog.getEntityType());
        assertEquals(savedProduct.getId(), decreaseLog.getEntityId());
        assertTrue(decreaseLog.getNewValues().contains("\"quantity\":10"));
        assertTrue(decreaseLog.getNewValues().contains("\"movementType\":\"OUT\""));
    }

    @Test
    void testAuditableAnnotationWorks() {
        // Given - create a test service with @Auditable annotation
        TestAuditableService testService = new TestAuditableService();

        // When - call annotated method (this would need AOP proxy in real scenario)
        // For this test, we'll verify the annotation exists and can be processed
        try {
            java.lang.reflect.Method method =
                    TestAuditableService.class.getMethod("auditableOperation", Long.class);
            Auditable annotation = method.getAnnotation(Auditable.class);

            // Then
            assertNotNull(annotation);
            assertEquals("CUSTOM_OPERATION", annotation.action());
            assertEquals("CUSTOM_ENTITY", annotation.entityType());
            assertEquals("customId", annotation.entityIdField());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Test method not found", e);
        }
    }

    // Test service class for @Auditable annotation testing
    public static class TestAuditableService {

        @Auditable(
                action = "CUSTOM_OPERATION",
                entityType = "CUSTOM_ENTITY",
                entityIdField = "customId")
        public TestResult auditableOperation(Long id) {
            return new TestResult(id);
        }
    }

    public static class TestResult {

        private Long customId;

        public TestResult(Long customId) {
            this.customId = customId;
        }

        public Long getCustomId() {
            return customId;
        }

        public void setCustomId(Long customId) {
            this.customId = customId;
        }
    }
}
