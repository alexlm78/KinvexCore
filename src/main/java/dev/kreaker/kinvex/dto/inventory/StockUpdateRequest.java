package dev.kreaker.kinvex.dto.inventory;

import dev.kreaker.kinvex.entity.InventoryMovement.ReferenceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** DTO para solicitudes de actualización de stock. */
public class StockUpdateRequest {

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer quantity;

    private ReferenceType referenceType;

    private Long referenceId;

    @Size(max = 50, message = "El sistema origen no puede exceder 50 caracteres")
    private String sourceSystem;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notes;

    // Default constructor
    public StockUpdateRequest() {}

    // Constructor with required fields
    public StockUpdateRequest(Integer quantity) {
        this.quantity = quantity;
    }

    // Constructor with common fields
    public StockUpdateRequest(
            Integer quantity, ReferenceType referenceType, Long referenceId, String sourceSystem) {
        this.quantity = quantity;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.sourceSystem = sourceSystem;
    }

    // Getters and Setters
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
}
