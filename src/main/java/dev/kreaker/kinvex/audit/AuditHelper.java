package dev.kreaker.kinvex.audit;

import dev.kreaker.kinvex.entity.AuditLog;
import dev.kreaker.kinvex.service.AuditService;
import org.springframework.stereotype.Component;

/**
 * Clase de utilidad para facilitar el registro de auditoría desde controladores y otros
 * componentes.
 *
 * <p>Proporciona métodos de conveniencia para operaciones comunes de auditoría.
 */
@Component
public class AuditHelper {

    private final AuditService auditService;

    public AuditHelper(AuditService auditService) {
        this.auditService = auditService;
    }

    /** Registra una operación de creación de producto. */
    public void logProductCreated(Long productId, Object productData) {
        auditService.logOperation(
                AuditLog.ACTION_CREATE, AuditLog.ENTITY_PRODUCT, productId, null, productData);
    }

    /** Registra una operación de actualización de producto. */
    public void logProductUpdated(Long productId, Object oldData, Object newData) {
        auditService.logOperation(
                AuditLog.ACTION_UPDATE, AuditLog.ENTITY_PRODUCT, productId, oldData, newData);
    }

    /** Registra una operación de eliminación de producto. */
    public void logProductDeleted(Long productId, Object productData) {
        auditService.logOperation(
                AuditLog.ACTION_DELETE, AuditLog.ENTITY_PRODUCT, productId, productData, null);
    }

    /** Registra una operación de creación de orden de compra. */
    public void logOrderCreated(Long orderId, Object orderData) {
        auditService.logOperation(
                AuditLog.ACTION_CREATE, AuditLog.ENTITY_PURCHASE_ORDER, orderId, null, orderData);
    }

    /** Registra una operación de actualización de orden de compra. */
    public void logOrderUpdated(Long orderId, Object oldData, Object newData) {
        auditService.logOperation(
                AuditLog.ACTION_UPDATE, AuditLog.ENTITY_PURCHASE_ORDER, orderId, oldData, newData);
    }

    /** Registra una operación de recepción de orden. */
    public void logOrderReceived(Long orderId, Object receptionData) {
        auditService.logOperation(
                AuditLog.ACTION_ORDER_RECEIVE,
                AuditLog.ENTITY_PURCHASE_ORDER,
                orderId,
                null,
                receptionData);
    }

    /** Registra un incremento de stock. */
    public void logStockIncrease(Long productId, Integer quantity, String source) {
        auditService.logInventoryMovement(
                AuditLog.ACTION_STOCK_INCREASE, productId, quantity, "IN", source);
    }

    /** Registra una disminución de stock. */
    public void logStockDecrease(Long productId, Integer quantity, String source) {
        auditService.logInventoryMovement(
                AuditLog.ACTION_STOCK_DECREASE, productId, quantity, "OUT", source);
    }

    /** Registra un login exitoso. */
    public void logSuccessfulLogin(String username) {
        auditService.logAuthenticationOperation(AuditLog.ACTION_LOGIN, username, true);
    }

    /** Registra un login fallido. */
    public void logFailedLogin(String username) {
        auditService.logAuthenticationOperation(AuditLog.ACTION_LOGIN, username, false);
    }

    /** Registra un logout. */
    public void logLogout(String username) {
        auditService.logAuthenticationOperation(AuditLog.ACTION_LOGOUT, username, true);
    }

    /** Registra una operación personalizada. */
    public void logCustomOperation(String action, String entityType, Long entityId, Object data) {
        auditService.logOperation(action, entityType, entityId, null, data);
    }

    /** Registra una operación personalizada con valores anteriores y nuevos. */
    public void logCustomOperation(
            String action, String entityType, Long entityId, Object oldData, Object newData) {
        auditService.logOperation(action, entityType, entityId, oldData, newData);
    }
}
