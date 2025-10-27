package dev.kreaker.kinvex.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.kreaker.kinvex.entity.AuditLog;
import dev.kreaker.kinvex.service.AuditService;
import dev.kreaker.kinvex.service.InventoryService;
import dev.kreaker.kinvex.service.OrderService;
import java.lang.reflect.Method;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    @Mock private AuditService auditService;

    @Mock private JoinPoint joinPoint;

    @Mock private MethodSignature methodSignature;

    private AuditAspect auditAspect;

    @BeforeEach
    void setUp() {
        auditAspect = new AuditAspect(auditService);
    }

    @Test
    void testAuditableAnnotationExists() throws Exception {
        // Test that the @Auditable annotation can be processed correctly
        Method method = TestService.class.getMethod("auditableMethod");
        Auditable annotation = method.getAnnotation(Auditable.class);

        assertNotNull(annotation);
        assertEquals("CUSTOM_ACTION", annotation.action());
        assertEquals("CUSTOM_ENTITY", annotation.entityType());
        assertEquals("id", annotation.entityIdField());
    }

    @Test
    void testAuditCreateMethod() {
        // Given
        InventoryService inventoryService = new InventoryService(null, null, null, null);
        TestResult result = new TestResult(789L);

        when(joinPoint.getTarget()).thenReturn(inventoryService);

        // When
        auditAspect.auditCreateMethod(joinPoint, result);

        // Then
        verify(auditService)
                .logOperation(
                        eq(AuditLog.ACTION_CREATE),
                        eq(AuditLog.ENTITY_PRODUCT),
                        eq(789L),
                        eq(null),
                        eq(result));
    }

    @Test
    void testAuditUpdateMethod() {
        // Given
        OrderService orderService = new OrderService(null, null, null, null, null, null);
        TestResult result = new TestResult(101L);

        when(joinPoint.getTarget()).thenReturn(orderService);

        // When
        auditAspect.auditUpdateMethod(joinPoint, result);

        // Then
        verify(auditService)
                .logOperation(
                        eq(AuditLog.ACTION_UPDATE),
                        eq(AuditLog.ENTITY_PURCHASE_ORDER),
                        eq(101L),
                        eq(null),
                        eq(result));
    }

    @Test
    void testAuditDeleteMethod() {
        // Given
        InventoryService inventoryService = new InventoryService(null, null, null, null);
        Object[] args = {202L};

        when(joinPoint.getTarget()).thenReturn(inventoryService);
        when(joinPoint.getArgs()).thenReturn(args);

        // When
        auditAspect.auditDeleteMethod(joinPoint);

        // Then
        verify(auditService)
                .logOperation(eq(AuditLog.ACTION_DELETE), eq(AuditLog.ENTITY_PRODUCT), eq(202L));
    }

    @Test
    void testAuditStockMethod_IncreaseStock() {
        // Given
        Object[] args = {303L, new StockRequest(15)};

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getName()).thenReturn("increaseStock");
        when(joinPoint.getArgs()).thenReturn(args);

        // When
        auditAspect.auditStockMethod(joinPoint, null);

        // Then
        verify(auditService)
                .logInventoryMovement(
                        eq(AuditLog.ACTION_STOCK_INCREASE),
                        eq(303L),
                        eq(15),
                        eq("IN"),
                        eq("SYSTEM"));
    }

    @Test
    void testAuditStockMethod_DecreaseStock() {
        // Given
        Object[] args = {404L, new StockRequest(25)};

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getName()).thenReturn("decreaseStock");
        when(joinPoint.getArgs()).thenReturn(args);

        // When
        auditAspect.auditStockMethod(joinPoint, null);

        // Then
        verify(auditService)
                .logInventoryMovement(
                        eq(AuditLog.ACTION_STOCK_DECREASE),
                        eq(404L),
                        eq(25),
                        eq("OUT"),
                        eq("SYSTEM"));
    }

    @Test
    void testAuditReceiveOrderMethod() {
        // Given
        Object[] args = {505L, new Object()};
        TestResult result = new TestResult(505L);

        when(joinPoint.getArgs()).thenReturn(args);

        // When
        auditAspect.auditReceiveOrderMethod(joinPoint, result);

        // Then
        verify(auditService)
                .logOperation(
                        eq(AuditLog.ACTION_ORDER_RECEIVE),
                        eq(AuditLog.ENTITY_PURCHASE_ORDER),
                        eq(505L),
                        eq(null),
                        eq(result));
    }

    @Test
    void testDeriveEntityTypeFromClass() {
        // Test the entity type derivation logic
        AuditAspect aspect = new AuditAspect(auditService);

        // We can't directly test private methods, but we can test the behavior
        // through the public methods that use them
        InventoryService inventoryService = new InventoryService(null, null, null, null);
        when(joinPoint.getTarget()).thenReturn(inventoryService);

        auditAspect.auditCreateMethod(joinPoint, new TestResult(1L));

        // Verify that the correct entity type was derived
        verify(auditService)
                .logOperation(
                        eq(AuditLog.ACTION_CREATE),
                        eq(AuditLog.ENTITY_PRODUCT), // Should derive "Product" from
                        // InventoryService
                        eq(1L),
                        eq(null),
                        any());
    }

    // Test helper classes
    public static class TestService {

        @Auditable(action = "CUSTOM_ACTION", entityType = "CUSTOM_ENTITY")
        public TestResult auditableMethod() {
            return new TestResult(123L);
        }

        @Auditable
        public TestResult defaultAuditableMethod() {
            return new TestResult(456L);
        }
    }

    public static class TestResult {

        private Long id;

        public TestResult(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    public static class StockRequest {

        private Integer quantity;

        public StockRequest(Integer quantity) {
            this.quantity = quantity;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
