package dev.kreaker.kinvex.dto.order;

import dev.kreaker.kinvex.entity.PurchaseOrder.OrderStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para la recepción de una orden de compra.
 */
public class OrderReceiptResponse {

    private Long orderId;
    private String orderNumber;
    private OrderStatus status;
    private LocalDate receivedDate;
    private LocalDateTime processedAt;
    private String notes;
    private List<OrderDetailReceiptResponse> receivedDetails;
    private boolean fullyReceived;

    // Default constructor
    public OrderReceiptResponse() {
    }

    // Constructor with all fields
    public OrderReceiptResponse(Long orderId, String orderNumber, OrderStatus status,
            LocalDate receivedDate, LocalDateTime processedAt, String notes,
            List<OrderDetailReceiptResponse> receivedDetails, boolean fullyReceived) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.status = status;
        this.receivedDate = receivedDate;
        this.processedAt = processedAt;
        this.notes = notes;
        this.receivedDetails = receivedDetails;
        this.fullyReceived = fullyReceived;
    }

    // Static factory method for success response
    public static OrderReceiptResponse success(Long orderId, String orderNumber, OrderStatus status,
            LocalDate receivedDate, String notes,
            List<OrderDetailReceiptResponse> receivedDetails,
            boolean fullyReceived) {
        return new OrderReceiptResponse(orderId, orderNumber, status, receivedDate,
                LocalDateTime.now(), notes, receivedDetails, fullyReceived);
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<OrderDetailReceiptResponse> getReceivedDetails() {
        return receivedDetails;
    }

    public void setReceivedDetails(List<OrderDetailReceiptResponse> receivedDetails) {
        this.receivedDetails = receivedDetails;
    }

    public boolean isFullyReceived() {
        return fullyReceived;
    }

    public void setFullyReceived(boolean fullyReceived) {
        this.fullyReceived = fullyReceived;
    }

    @Override
    public String toString() {
        return "OrderReceiptResponse{"
                + "orderId=" + orderId
                + ", orderNumber='" + orderNumber + '\''
                + ", status=" + status
                + ", receivedDate=" + receivedDate
                + ", processedAt=" + processedAt
                + ", notes='" + notes + '\''
                + ", receivedDetails=" + receivedDetails
                + ", fullyReceived=" + fullyReceived
                + '}';
    }
}
