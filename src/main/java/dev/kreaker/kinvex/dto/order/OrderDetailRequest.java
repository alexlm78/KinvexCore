package dev.kreaker.kinvex.dto.order;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para los detalles de productos en una orden de compra.
 */
public class OrderDetailRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @NotNull(message = "La cantidad ordenada es obligatoria")
    @Min(value = 1, message = "La cantidad ordenada debe ser mayor a 0")
    private Integer quantityOrdered;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio unitario debe ser mayor a 0")
    private BigDecimal unitPrice;

    // Default constructor
    public OrderDetailRequest() {
    }

    // Constructor with all fields
    public OrderDetailRequest(Long productId, Integer quantityOrdered, BigDecimal unitPrice) {
        this.productId = productId;
        this.quantityOrdered = quantityOrdered;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(Integer quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    @Override
    public String toString() {
        return "OrderDetailRequest{"
                + "productId=" + productId
                + ", quantityOrdered=" + quantityOrdered
                + ", unitPrice=" + unitPrice
                + '}';
    }
}
