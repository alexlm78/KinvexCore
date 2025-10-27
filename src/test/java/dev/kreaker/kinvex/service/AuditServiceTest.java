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
        List<AuditLog> expectedHistory = List.of(new AuditLog(), new AuditLog());

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
        List<Object[]> expectedStats =
                List.of(
                        new Object[] {AuditLog.ACTION_CREATE, 5L},
                        new Object[] {AuditLog.ACTION_UPDATE, 3L});

        when(auditLogRepository.findActionStatisticsBetween(startDate, endDate))
                .thenReturn(expectedStats);

        // When
        List<Object[]> result = auditService.getActionStatistics(startDate, endDate);

        // Then
        assertEquals(expectedStats, result);
        verify(auditLogRepository).findActionStatisticsBetween(startDate, endDate);
    }
}
