package dev.kreaker.kinvex.dto.order;

import dev.kreaker.kinvex.entity.PurchaseOrder.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para actualizar el estado de una orden de compra. Implementa el requerimiento 3.4: Actualizar
 * el estado de las órdenes de compra.
 */
public class UpdateOrderStatusRequest {

    @NotNull(message = "El estado es obligatorio")
    private OrderStatus status;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notes;

    // Default constructor
    public UpdateOrderStatusRequest() {}

    // Constructor with required fields
    public UpdateOrderStatusRequest(OrderStatus status) {
        this.status = status;
    }

    // Constructor with all fields
    public UpdateOrderStatusRequest(OrderStatus status, String notes) {
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters
    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "UpdateOrderStatusRequest{" + "status=" + status + ", notes='" + notes + '\'' + '}';
    }
}
