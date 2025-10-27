package dev.kreaker.kinvex.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import dev.kreaker.kinvex.entity.InventoryMovement.MovementType;
import dev.kreaker.kinvex.entity.InventoryMovement.ReferenceType;

/**
 * DTO for inventory movement reports Requirement 4.1: Generate reports of
 * inventory movements by time period
 */
public class InventoryMovementReportDto {

    private Long movementId;
    private String productCode;
    private String productName;
    private MovementType movementType;
    private Integer quantity;
    private ReferenceType referenceType;
    private Long referenceId;
    private String sourceSystem;
    private String notes;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private BigDecimal unitPrice;
    private BigDecimal totalValue;

    // Default constructor
    public InventoryMovementReportDto() {
    }

    // Constructor with essential fields
    public InventoryMovementReportDto(
            Long movementId,
            String productCode,
            String productName,
            MovementType movementType,
            Integer quantity,
            LocalDateTime createdAt,
            BigDecimal unitPrice) {
        this.movementId = movementId;
        this.productCode = productCode;
        this.productName = productName;
        this.movementType = movementType;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.unitPrice = unitPrice;
        this.totalValue = unitPrice != null && quantity != null
                ? unitPrice.multiply(BigDecimal.valueOf(quantity))
                : BigDecimal.ZERO;
    }

    // Full constructor
    public InventoryMovementReportDto(
            Long movementId,
            String productCode,
            String productName,
            MovementType movementType,
            Integer quantity,
            ReferenceType referenceType,
            Long referenceId,
            String sourceSystem,
            String notes,
            String createdByUsername,
            LocalDateTime createdAt,
            BigDecimal unitPrice) {
        this(movementId, productCode, productName, movementType, quantity, createdAt, unitPrice);
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.sourceSystem = sourceSystem;
        this.notes = notes;
        this.createdByUsername = createdByUsername;
    }

    // Getters and Setters
    public Long getMovementId() {
        return movementId;
    }

    public void setMovementId(Long movementId) {
        this.movementId = movementId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        updateTotalValue();
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(ReferenceType referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        updateTotalValue();
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    // Helper methods
    private void updateTotalValue() {
        if (unitPrice != null && quantity != null) {
            this.totalValue = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public boolean isInbound() {
        return movementType == MovementType.IN;
    }

    public boolean isOutbound() {
        return movementType == MovementType.OUT;
    }

    @Override
    public String toString() {
        return "InventoryMovementReportDto{"
                + "movementId=" + movementId
                + ", productCode='" + productCode + '\''
                + ", productName='" + productName + '\''
                + ", movementType=" + movementType
                + ", quantity=" + quantity
                + ", totalValue=" + totalValue
                + ", createdAt=" + createdAt
                + '}';
    }
}
