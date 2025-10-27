package dev.kreaker.kinvex.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** DTO para registrar la cantidad recibida de un producto específico en una orden. */
public class OrderDetailReceiptRequest {

    @NotNull(message = "El ID del detalle de orden es obligatorio")
    private Long orderDetailId;

    @NotNull(message = "La cantidad recibida es obligatoria")
    @Min(value = 0, message = "La cantidad recibida no puede ser negativa")
    private Integer quantityReceived;

    // Default constructor
    public OrderDetailReceiptRequest() {}

    // Constructor with all fields
    public OrderDetailReceiptRequest(Long orderDetailId, Integer quantityReceived) {
        this.orderDetailId = orderDetailId;
        this.quantityReceived = quantityReceived;
    }

    // Getters and Setters
    public Long getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(Long orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public Integer getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(Integer quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    @Override
    public String toString() {
        return "OrderDetailReceiptRequest{"
                + "orderDetailId="
                + orderDetailId
                + ", quantityReceived="
                + quantityReceived
                + '}';
    }
}
