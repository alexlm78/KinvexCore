package dev.kreaker.kinvex.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "inventory_movements")
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private MovementType movementType;

    @NotNull
    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 20)
    private ReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Size(max = 50)
    @Column(name = "source_system", length = 50)
    private String sourceSystem;

    @Size(max = 500)
    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default constructor
    public InventoryMovement() {
    }

    // Constructor with required fields
    public InventoryMovement(Product product, MovementType movementType, Integer quantity) {
        this.product = product;
        this.movementType = movementType;
        this.quantity = quantity;
    }

    // Constructor with common fields
    public InventoryMovement(
            Product product,
            MovementType movementType,
            Integer quantity,
            ReferenceType referenceType,
            Long referenceId,
            User createdBy) {
        this.product = product;
        this.movementType = movementType;
        this.quantity = quantity;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Business methods
    public boolean isInbound() {
        return movementType == MovementType.IN;
    }

    public boolean isOutbound() {
        return movementType == MovementType.OUT;
    }

    public Integer getSignedQuantity() {
        return movementType == MovementType.IN ? quantity : -quantity;
    }

    @Override
    public String toString() {
        return "InventoryMovement{"
                + "id="
                + id
                + ", productId="
                + (product != null ? product.getId() : null)
                + ", movementType="
                + movementType
                + ", quantity="
                + quantity
                + ", referenceType="
                + referenceType
                + ", referenceId="
                + referenceId
                + ", sourceSystem='"
                + sourceSystem
                + '\''
                + ", createdAt="
                + createdAt
                + '}';
    }

    public enum MovementType {
        IN,
        OUT
    }

    public enum ReferenceType {
        PURCHASE_ORDER,
        SALE,
        ADJUSTMENT,
        TRANSFER,
        RETURN
    }
}
