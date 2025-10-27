package dev.kreaker.kinvex.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "order_number", unique = true, nullable = false, length = 50)
    private String orderNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @NotNull
    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "expected_date")
    private LocalDate expectedDate;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Size(max = 1000)
    @Column(length = 1000)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("order-orderDetails")
    private List<OrderDetail> orderDetails = new ArrayList<>();

    // Default constructor
    public PurchaseOrder() {}

    // Constructor with required fields
    public PurchaseOrder(String orderNumber, Supplier supplier, LocalDate orderDate) {
        this.orderNumber = orderNumber;
        this.supplier = supplier;
        this.orderDate = orderDate;
    }

    // Constructor with common fields
    public PurchaseOrder(
            String orderNumber,
            Supplier supplier,
            LocalDate orderDate,
            LocalDate expectedDate,
            User createdBy) {
        this.orderNumber = orderNumber;
        this.supplier = supplier;
        this.orderDate = orderDate;
        this.expectedDate = expectedDate;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    // Helper methods
    public void addOrderDetail(OrderDetail orderDetail) {
        orderDetails.add(orderDetail);
        orderDetail.setOrder(this);
        calculateTotalAmount();
    }

    public void removeOrderDetail(OrderDetail orderDetail) {
        orderDetails.remove(orderDetail);
        orderDetail.setOrder(null);
        calculateTotalAmount();
    }

    public void calculateTotalAmount() {
        this.totalAmount =
                orderDetails.stream()
                        .map(OrderDetail::getTotalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isOverdue() {
        return expectedDate != null
                && LocalDate.now().isAfter(expectedDate)
                && status != OrderStatus.COMPLETED
                && status != OrderStatus.CANCELLED;
    }

    public boolean isFullyReceived() {
        return orderDetails.stream()
                .allMatch(detail -> detail.getQuantityReceived() >= detail.getQuantityOrdered());
    }

    public boolean isPartiallyReceived() {
        return orderDetails.stream().anyMatch(detail -> detail.getQuantityReceived() > 0)
                && !isFullyReceived();
    }

    @Override
    public String toString() {
        return "PurchaseOrder{"
                + "id="
                + id
                + ", orderNumber='"
                + orderNumber
                + '\''
                + ", supplierId="
                + (supplier != null ? supplier.getId() : null)
                + ", status="
                + status
                + ", orderDate="
                + orderDate
                + ", expectedDate="
                + expectedDate
                + ", totalAmount="
                + totalAmount
                + ", createdAt="
                + createdAt
                + '}';
    }

    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PARTIAL,
        COMPLETED,
        CANCELLED
    }
}
