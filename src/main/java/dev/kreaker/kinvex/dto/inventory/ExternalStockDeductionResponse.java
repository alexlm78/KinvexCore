package dev.kreaker.kinvex.dto.inventory;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para operaciones de descuento de stock desde sistemas externos.
 *
 * <p>Implementa el requerimiento 2.4: Registrar cada movimiento de salida con timestamp, producto,
 * cantidad y sistema origen.
 */
public class ExternalStockDeductionResponse {

    private String productCode;
    private String productName;
    private Integer quantityDeducted;
    private Integer previousStock;
    private Integer currentStock;
    private String sourceSystem;
    private LocalDateTime timestamp;
    private Long movementId;
    private String status;
    private String message;

    // Default constructor
    public ExternalStockDeductionResponse() {}

    // Constructor for successful deduction
    public ExternalStockDeductionResponse(
            String productCode,
            String productName,
            Integer quantityDeducted,
            Integer previousStock,
            Integer currentStock,
            String sourceSystem,
            LocalDateTime timestamp,
            Long movementId) {
        this.productCode = productCode;
        this.productName = productName;
        this.quantityDeducted = quantityDeducted;
        this.previousStock = previousStock;
        this.currentStock = currentStock;
        this.sourceSystem = sourceSystem;
        this.timestamp = timestamp;
        this.movementId = movementId;
        this.status = "SUCCESS";
        this.message = "Stock deducido exitosamente";
    }

    // Static factory method for success response
    public static ExternalStockDeductionResponse success(
            String productCode,
            String productName,
            Integer quantityDeducted,
            Integer previousStock,
            Integer currentStock,
            String sourceSystem,
            LocalDateTime timestamp,
            Long movementId) {
        return new ExternalStockDeductionResponse(
                productCode,
                productName,
                quantityDeducted,
                previousStock,
                currentStock,
                sourceSystem,
                timestamp,
                movementId);
    }

    // Static factory method for error response
    public static ExternalStockDeductionResponse error(String productCode, String message) {
        ExternalStockDeductionResponse response = new ExternalStockDeductionResponse();
        response.productCode = productCode;
        response.status = "ERROR";
        response.message = message;
        response.timestamp = LocalDateTime.now();
        return response;
    }

    // Getters and Setters
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

    public Integer getQuantityDeducted() {
        return quantityDeducted;
    }

    public void setQuantityDeducted(Integer quantityDeducted) {
        this.quantityDeducted = quantityDeducted;
    }

    public Integer getPreviousStock() {
        return previousStock;
    }

    public void setPreviousStock(Integer previousStock) {
        this.previousStock = previousStock;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getMovementId() {
        return movementId;
    }

    public void setMovementId(Long movementId) {
        this.movementId = movementId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ExternalStockDeductionResponse{"
                + "productCode='"
                + productCode
                + '\''
                + ", productName='"
                + productName
                + '\''
                + ", quantityDeducted="
                + quantityDeducted
                + ", previousStock="
                + previousStock
                + ", currentStock="
                + currentStock
                + ", sourceSystem='"
                + sourceSystem
                + '\''
                + ", timestamp="
                + timestamp
                + ", movementId="
                + movementId
                + ", status='"
                + status
                + '\''
                + ", message='"
                + message
                + '\''
                + '}';
    }
}
