package dev.kreaker.kinvex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String action;

    @NotBlank
    @Size(max = 50)
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Size(max = 500)
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default constructor
    public AuditLog() {}

    // Constructor with required fields
    public AuditLog(String action, String entityType) {
        this.action = action;
        this.entityType = entityType;
    }

    // Constructor with common fields
    public AuditLog(
            User user,
            String action,
            String entityType,
            Long entityId,
            String oldValues,
            String newValues) {
        this.user = user;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.oldValues = oldValues;
        this.newValues = newValues;
    }

    // Constructor with all fields
    public AuditLog(
            User user,
            String action,
            String entityType,
            Long entityId,
            String oldValues,
            String newValues,
            String ipAddress,
            String userAgent) {
        this.user = user;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getOldValues() {
        return oldValues;
    }

    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }

    public String getNewValues() {
        return newValues;
    }

    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Business methods
    public boolean isCreateAction() {
        return "CREATE".equalsIgnoreCase(action);
    }

    public boolean isUpdateAction() {
        return "UPDATE".equalsIgnoreCase(action);
    }

    public boolean isDeleteAction() {
        return "DELETE".equalsIgnoreCase(action);
    }

    @Override
    public String toString() {
        return "AuditLog{"
                + "id="
                + id
                + ", userId="
                + (user != null ? user.getId() : null)
                + ", action='"
                + action
                + '\''
                + ", entityType='"
                + entityType
                + '\''
                + ", entityId="
                + entityId
                + ", ipAddress="
                + ipAddress
                + ", createdAt="
                + createdAt
                + '}';
    }

    // Common audit actions as constants
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_STOCK_INCREASE = "STOCK_INCREASE";
    public static final String ACTION_STOCK_DECREASE = "STOCK_DECREASE";
    public static final String ACTION_ORDER_RECEIVE = "ORDER_RECEIVE";

    // Common entity types as constants
    public static final String ENTITY_USER = "User";
    public static final String ENTITY_PRODUCT = "Product";
    public static final String ENTITY_CATEGORY = "Category";
    public static final String ENTITY_SUPPLIER = "Supplier";
    public static final String ENTITY_PURCHASE_ORDER = "PurchaseOrder";
    public static final String ENTITY_ORDER_DETAIL = "OrderDetail";
    public static final String ENTITY_INVENTORY_MOVEMENT = "InventoryMovement";
}
