package dev.kreaker.kinvex.dto.alert;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.kreaker.kinvex.entity.PurchaseOrder;
import java.math.BigDecimal;
import java.time.LocalDate;

/** DTO para respuesta de alertas de órdenes. */
public class OrderAlertResponse {

    private Long orderId;
    private String orderNumber;
    private String supplierName;
    private PurchaseOrder.OrderStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate orderDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receivedDate;

    private BigDecimal totalAmount;
    private Long daysOverdue;
    private Long daysUntilDue;
    private String alertType;
    private String alertMessage;

    // Constructors
    public OrderAlertResponse() {}

    public OrderAlertResponse(PurchaseOrder order, String alertType, String alertMessage) {
        this.orderId = order.getId();
        this.orderNumber = order.getOrderNumber();
        this.supplierName = order.getSupplier().getName();
        this.status = order.getStatus();
        this.orderDate = order.getOrderDate();
        this.expectedDate = order.getExpectedDate();
        this.receivedDate = order.getReceivedDate();
        this.totalAmount = order.getTotalAmount();
        this.alertType = alertType;
        this.alertMessage = alertMessage;

        // Calcular días vencidos o restantes
        if (order.getExpectedDate() != null) {
            long daysDifference =
                    LocalDate.now().toEpochDay() - order.getExpectedDate().toEpochDay();
            if (daysDifference > 0) {
                this.daysOverdue = daysDifference;
                this.daysUntilDue = null;
            } else {
                this.daysOverdue = null;
                this.daysUntilDue = Math.abs(daysDifference);
            }
        }
    }

    // Static factory methods
    public static OrderAlertResponse forOverdueOrder(PurchaseOrder order, long daysOverdue) {
        String message = String.format("Orden vencida hace %d días", daysOverdue);
        OrderAlertResponse response = new OrderAlertResponse(order, "OVERDUE", message);
        response.setDaysOverdue(daysOverdue);
        return response;
    }

    public static OrderAlertResponse forOrderDueSoon(PurchaseOrder order, long daysUntilDue) {
        String message = String.format("Orden vence en %d días", daysUntilDue);
        OrderAlertResponse response = new OrderAlertResponse(order, "DUE_SOON", message);
        response.setDaysUntilDue(daysUntilDue);
        return response;
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

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public PurchaseOrder.OrderStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseOrder.OrderStatus status) {
        this.status = status;
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

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getDaysOverdue() {
        return daysOverdue;
    }

    public void setDaysOverdue(Long daysOverdue) {
        this.daysOverdue = daysOverdue;
    }

    public Long getDaysUntilDue() {
        return daysUntilDue;
    }

    public void setDaysUntilDue(Long daysUntilDue) {
        this.daysUntilDue = daysUntilDue;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    @Override
    public String toString() {
        return "OrderAlertResponse{"
                + "orderId="
                + orderId
                + ", orderNumber='"
                + orderNumber
                + '\''
                + ", supplierName='"
                + supplierName
                + '\''
                + ", status="
                + status
                + ", expectedDate="
                + expectedDate
                + ", daysOverdue="
                + daysOverdue
                + ", daysUntilDue="
                + daysUntilDue
                + ", alertType='"
                + alertType
                + '\''
                + '}';
    }
}
