package dev.kreaker.kinvex.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para registrar la recepción de productos de una orden de compra. Implementa el requerimiento
 * 3.2: Registrar la recepción parcial o total de productos de una orden de compra.
 */
public class ReceiveOrderRequest {

    private LocalDate receivedDate;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notes;

    @NotEmpty(message = "Debe especificar al menos un producto recibido")
    @Valid
    private List<OrderDetailReceiptRequest> receivedDetails;

    // Default constructor
    public ReceiveOrderRequest() {}

    // Constructor with required fields
    public ReceiveOrderRequest(List<OrderDetailReceiptRequest> receivedDetails) {
        this.receivedDetails = receivedDetails;
        this.receivedDate = LocalDate.now();
    }

    // Constructor with all fields
    public ReceiveOrderRequest(
            LocalDate receivedDate, String notes, List<OrderDetailReceiptRequest> receivedDetails) {
        this.receivedDate = receivedDate;
        this.notes = notes;
        this.receivedDetails = receivedDetails;
    }

    // Getters and Setters
    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<OrderDetailReceiptRequest> getReceivedDetails() {
        return receivedDetails;
    }

    public void setReceivedDetails(List<OrderDetailReceiptRequest> receivedDetails) {
        this.receivedDetails = receivedDetails;
    }

    @Override
    public String toString() {
        return "ReceiveOrderRequest{"
                + "receivedDate="
                + receivedDate
                + ", notes='"
                + notes
                + '\''
                + ", receivedDetails="
                + receivedDetails
                + '}';
    }
}
