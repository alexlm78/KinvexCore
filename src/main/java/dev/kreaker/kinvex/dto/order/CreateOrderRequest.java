package dev.kreaker.kinvex.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para crear una nueva orden de compra. Implementa el requerimiento 3.1: Crear órdenes de
 * compra especificando proveedor, productos, cantidades y fechas esperadas.
 */
public class CreateOrderRequest {

    @NotNull(message = "El ID del proveedor es obligatorio")
    private Long supplierId;

    @NotBlank(message = "El número de orden es obligatorio")
    @Size(max = 50, message = "El número de orden no puede exceder 50 caracteres")
    private String orderNumber;

    @NotNull(message = "La fecha de orden es obligatoria")
    private LocalDate orderDate;

    private LocalDate expectedDate;

    @Size(max = 1000, message = "Las notas no pueden exceder 1000 caracteres")
    private String notes;

    @NotEmpty(message = "La orden debe contener al menos un producto")
    @Valid
    private List<OrderDetailRequest> orderDetails;

    // Default constructor
    public CreateOrderRequest() {}

    // Constructor with required fields
    public CreateOrderRequest(
            Long supplierId,
            String orderNumber,
            LocalDate orderDate,
            List<OrderDetailRequest> orderDetails) {
        this.supplierId = supplierId;
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.orderDetails = orderDetails;
    }

    // Getters and Setters
    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(LocalDate expectedDate) {
        this.expectedDate = expectedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<OrderDetailRequest> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetailRequest> orderDetails) {
        this.orderDetails = orderDetails;
    }

    @Override
    public String toString() {
        return "CreateOrderRequest{"
                + "supplierId="
                + supplierId
                + ", orderNumber='"
                + orderNumber
                + '\''
                + ", orderDate="
                + orderDate
                + ", expectedDate="
                + expectedDate
                + ", notes='"
                + notes
                + '\''
                + ", orderDetails="
                + orderDetails
                + '}';
    }
}
