package dev.kreaker.kinvex.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kreaker.kinvex.entity.AuditLog;
import dev.kreaker.kinvex.entity.User;
import dev.kreaker.kinvex.entity.User.UserRole;
import dev.kreaker.kinvex.repository.AuditLogRepository;
import dev.kreaker.kinvex.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock private AuditLogRepository auditLogRepository;

    @Mock private UserRepository userRepository;

    @Mock private ObjectMapper objectMapper;

    @Mock private SecurityContext securityContext;

    @Mock private Authentication authentication;

    private AuditService auditService;

    private User testUser;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(auditLogRepository, userRepository, objectMapper);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(UserRole.OPERATOR);
        testUser.setActive(true);

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testLogOperation_BasicOperation() {
        // Given
        String action = AuditLog.ACTION_CREATE;
        String entityType = AuditLog.ENTITY_PRODUCT;
        Long entityId = 123L;

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getPrincipal()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        auditService.logOperation(action, entityType, entityId);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedAuditLog = auditLogCaptor.getValue();
        assertEquals(action, savedAuditLog.getAction());
        assertEquals(entityType, savedAuditLog.getEntityType());
        assertEquals(entityId, savedAuditLog.getEntityId());
        assertEquals(testUser, savedAuditLog.getUser());
    }

    @Test
    void testLogOperation_WithValues() throws Exception {
        // Given
        String action = AuditLog.ACTION_UPDATE;
        String entityType = AuditLog.ENTITY_PRODUCT;
        Long entityId = 123L;
        Object oldValues = "old data";
        Object newValues = "new data";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getPrincipal()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(objectMapper.writeValueAsString(oldValues)).thenReturn("\"old data\"");
        when(objectMapper.writeValueAsString(newValues)).thenReturn("\"new data\"");

        // When
        auditService.logOperation(action, entityType, entityId, oldValues, newValues);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedAuditLog = auditLogCaptor.getValue();
        assertEquals(action, savedAuditLog.getAction());
        assertEquals(entityType, savedAuditLog.getEntityType());
        assertEquals(entityId, savedAuditLog.getEntityId());
        assertEquals(testUser, savedAuditLog.getUser());
        assertEquals("\"old data\"", savedAuditLog.getOldValues());
        assertEquals("\"new data\"", savedAuditLog.getNewValues());
    }

    @Test
    void testLogAuthenticationOperation_Success() {
        // Given
        String action = AuditLog.ACTION_LOGIN;
        String username = "testuser";
        boolean success = true;

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        auditService.logAuthenticationOperation(action, username, success);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedAuditLog = auditLogCaptor.getValue();
        assertEquals(action + "_SUCCESS", savedAuditLog.getAction());
        assertEquals(AuditLog.ENTITY_USER, savedAuditLog.getEntityType());
        assertEquals(testUser, savedAuditLog.getUser());
        assertEquals(testUser.getId(), savedAuditLog.getEntityId());
    }

    @Test
    void testLogAuthenticationOperation_Failure() {
        // Given
        String action = AuditLog.ACTION_LOGIN;
        String username = "nonexistentuser";
        boolean success = false;

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When
        auditService.logAuthenticationOperation(action, username, success);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedAuditLog = auditLogCaptor.getValue();
        assertEquals(action + "_FAILURE", savedAuditLog.getAction());
        assertEquals(AuditLog.ENTITY_USER, savedAuditLog.getEntityType());
        assertEquals("{\"username\":\"nonexistentuser\"}", savedAuditLog.getNewValues());
    }

    @Test
    void testLogInventoryMovement() {
        // Given
        String action = AuditLog.ACTION_STOCK_INCREASE;
        Long productId = 123L;
        Integer quantity = 10;
        String movementType = "IN";
        String sourceSystem = "SYSTEM";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getPrincipal()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        auditService.logInventoryMovement(action, productId, quantity, movementType, sourceSystem);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedAuditLog = auditLogCaptor.getValue();
        assertEquals(action, savedAuditLog.getAction());
        assertEquals(AuditLog.ENTITY_INVENTORY_MOVEMENT, savedAuditLog.getEntityType());
        assertEquals(productId, savedAuditLog.getEntityId());
        assertEquals(testUser, savedAuditLog.getUser());
        // The new values should contain the movement data
        String expectedMovementData =
                String.format(
                        "{\"quantity\":%d,\"movementType\":\"%s\",\"sourceSystem\":\"%s\"}",
                        quantity, movementType, sourceSystem);
        assertEquals(expectedMovementData, savedAuditLog.getNewValues());
    }

    @Test
    void testGetEntityHistory() {
        // Given
        String entityType = AuditLog.ENTITY_PRODUCT;
        Long entityId = 123L;
        List<AuditLog> expectedHistory = new java.util.ArrayList<>();
        expectedHistory.add(new AuditLog());
        expectedHistory.add(new AuditLog());

        when(auditLogRepository.findEntityHistory(entityType, entityId))
                .thenReturn(expectedHistory);

        // When
        List<AuditLog> result = auditService.getEntityHistory(entityType, entityId);

        // Then
        assertEquals(expectedHistory, result);
        verify(auditLogRepository).findEntityHistory(entityType, entityId);
    }

    @Test
    void testGetActionStatistics() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<Object[]> expectedStats = new java.util.ArrayList<>();
        expectedStats.add(new Object[] {AuditLog.ACTION_CREATE, 5L});
        expectedStats.add(new Object[] {AuditLog.ACTION_UPDATE, 3L});

        when(auditLogRepository.findActionStatisticsBetween(startDate, endDate))
                .thenReturn(expectedStats);

        // When
        List<Object[]> result = auditService.getActionStatistics(startDate, endDate);

        // Then
        assertEquals(expectedStats, result);
        verify(auditLogRepository).findActionStatisticsBetween(startDate, endDate);
    }

    @Test
    void testLogOperation_WithoutAuthentication() {
        // Given
        String action = AuditLog.ACTION_CREATE;
        String entityType = AuditLog.ENTITY_PRODUCT;
        Long entityId = 123L;

        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        auditService.logOperation(action, entityType, entityId);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedAuditLog = auditLogCaptor.getValue();
        assertEquals(action, savedAuditLog.getAction());
        assertEquals(entityType, savedAuditLog.getEntityType());
        assertEquals(entityId, savedAuditLog.getEntityId());
        assertEquals(null, savedAuditLog.getUser()); // No user when not authenticated
    }

    @Test
    void testLogOperation_WithAnonymousUser() {
        // Given
        String action = AuditLog.ACTION_DELETE;
        String entityType = AuditLog.ENTITY_PRODUCT;
        Long entityId = 456L;

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        // When
        auditService.logOperation(action, entityType, entityId);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedAuditLog = auditLogCaptor.getValue();
        assertEquals(action, savedAuditLog.getAction());
        assertEquals(entityType, savedAuditLog.getEntityType());
        assertEquals(entityId, savedAuditLog.getEntityId());
        assertEquals(null, savedAuditLog.getUser()); // No user for anonymous
    }

    @Test
    void testLogInventoryMovement_WithNullSourceSystem() {
        // Given
        String action = AuditLog.ACTION_STOCK_DECREASE;
        Long productId = 789L;
        Integer quantity = 5;
        String movementType = "OUT";
        String sourceSystem = null;

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getPrincipal()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        auditService.logInventoryMovement(action, productId, quantity, movementType, sourceSystem);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedAuditLog = auditLogCaptor.getValue();
        assertEquals(action, savedAuditLog.getAction());
        assertEquals(AuditLog.ENTITY_INVENTORY_MOVEMENT, savedAuditLog.getEntityType());
        assertEquals(productId, savedAuditLog.getEntityId());

        // Should default to "SYSTEM" when sourceSystem is null
        String expectedMovementData =
                String.format(
                        "{\"quantity\":%d,\"movementType\":\"%s\",\"sourceSystem\":\"%s\"}",
                        quantity, movementType, "SYSTEM");
        assertEquals(expectedMovementData, savedAuditLog.getNewValues());
    }

    @Test
    void testGetEntityTypeStatistics() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        List<Object[]> expectedStats = new java.util.ArrayList<>();
        expectedStats.add(new Object[] {AuditLog.ENTITY_PRODUCT, 15L});
        expectedStats.add(new Object[] {AuditLog.ENTITY_PURCHASE_ORDER, 8L});

        when(auditLogRepository.findEntityTypeStatisticsBetween(startDate, endDate))
                .thenReturn(expectedStats);

        // When
        List<Object[]> result = auditService.getEntityTypeStatistics(startDate, endDate);

        // Then
        assertEquals(expectedStats, result);
        verify(auditLogRepository).findEntityTypeStatisticsBetween(startDate, endDate);
    }

    @Test
    void testGetUserActivityStatistics() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<Object[]> expectedStats = new java.util.ArrayList<>();
        expectedStats.add(new Object[] {testUser, 25L});

        when(auditLogRepository.findUserActivityStatisticsBetween(startDate, endDate))
                .thenReturn(expectedStats);

        // When
        List<Object[]> result = auditService.getUserActivityStatistics(startDate, endDate);

        // Then
        assertEquals(expectedStats, result);
        verify(auditLogRepository).findUserActivityStatisticsBetween(startDate, endDate);
    }

    @Test
    void testGetLoginActivity() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        List<AuditLog> expectedActivity = new java.util.ArrayList<>();
        expectedActivity.add(
                new AuditLog(
                        testUser,
                        AuditLog.ACTION_LOGIN + "_SUCCESS",
                        AuditLog.ENTITY_USER,
                        testUser.getId(),
                        null,
                        null));
        expectedActivity.add(
                new AuditLog(
                        testUser,
                        AuditLog.ACTION_LOGOUT,
                        AuditLog.ENTITY_USER,
                        testUser.getId(),
                        null,
                        null));

        when(auditLogRepository.findLoginActivityBetween(startDate, endDate))
                .thenReturn(expectedActivity);

        // When
        List<AuditLog> result = auditService.getLoginActivity(startDate, endDate);

        // Then
        assertEquals(expectedActivity, result);
        verify(auditLogRepository).findLoginActivityBetween(startDate, endDate);
    }

    @Test
    void testLogOperation_HandlesJsonSerializationError() throws Exception {
        // Given
        String action = AuditLog.ACTION_UPDATE;
        String entityType = AuditLog.ENTITY_PRODUCT;
        Long entityId = 123L;
        Object oldValues = new Object();
        Object newValues = new Object();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getPrincipal()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Simulate JSON serialization error
        when(objectMapper.writeValueAsString(oldValues))
                .thenThrow(
                        new com.fasterxml.jackson.core.JsonProcessingException(
                                "Serialization error") {});
        when(objectMapper.writeValueAsString(newValues))
                .thenThrow(
                        new com.fasterxml.jackson.core.JsonProcessingException(
                                "Serialization error") {});

        // When
        auditService.logOperation(action, entityType, entityId, oldValues, newValues);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedAuditLog = auditLogCaptor.getValue();
        assertEquals(action, savedAuditLog.getAction());
        assertEquals(entityType, savedAuditLog.getEntityType());
        assertEquals(entityId, savedAuditLog.getEntityId());
        // Should fallback to toString() when JSON serialization fails
        assertEquals(oldValues.toString(), savedAuditLog.getOldValues());
        assertEquals(newValues.toString(), savedAuditLog.getNewValues());
    }
}
