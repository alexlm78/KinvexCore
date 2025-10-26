package dev.kreaker.kinvex.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO específico para solicitudes de descuento de stock desde sistemas externos de facturación.
 *
 * <p>Implementa el requerimiento 2.1: Endpoint REST para descuento de inventario que reciba código
 * de producto y cantidad.
 */
public class ExternalStockDeductionRequest {

    @NotBlank(message = "El código de producto es obligatorio")
    @Size(max = 50, message = "El código de producto no puede exceder 50 caracteres")
    private String productCode;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer quantity;

    @Size(max = 50, message = "El sistema origen no puede exceder 50 caracteres")
    private String sourceSystem;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notes;

    // Default constructor
    public ExternalStockDeductionRequest() {}

    // Constructor with required fields
    public ExternalStockDeductionRequest(String productCode, Integer quantity) {
        this.productCode = productCode;
        this.quantity = quantity;
    }

    // Constructor with all fields
    public ExternalStockDeductionRequest(
            String productCode, Integer quantity, String sourceSystem, String notes) {
        this.productCode = productCode;
        this.quantity = quantity;
        this.sourceSystem = sourceSystem;
        this.notes = notes;
    }

    // Getters and Setters
    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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

    @Override
    public String toString() {
        return "ExternalStockDeductionRequest{"
                + "productCode='"
                + productCode
                + '\''
                + ", quantity="
                + quantity
                + ", sourceSystem='"
                + sourceSystem
                + '\''
                + ", notes='"
                + notes
                + '\''
                + '}';
    }
}
