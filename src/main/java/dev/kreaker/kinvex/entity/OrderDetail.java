package dev.kreaker.kinvex.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "order_details")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference("order-orderDetails")
    private PurchaseOrder order;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Min(1)
    @Column(name = "quantity_ordered", nullable = false)
    private Integer quantityOrdered;

    @Min(0)
    @Column(name = "quantity_received", nullable = false)
    private Integer quantityReceived = 0;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    // Default constructor
    public OrderDetail() {
    }

    // Constructor with required fields
    public OrderDetail(
            PurchaseOrder order, Product product, Integer quantityOrdered, BigDecimal unitPrice) {
        this.order = order;
        this.product = product;
        this.quantityOrdered = quantityOrdered;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantityOrdered));
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PurchaseOrder getOrder() {
        return order;
    }

    public void setOrder(PurchaseOrder order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(Integer quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
        calculateTotalPrice();
    }

    public Integer getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(Integer quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    // Business methods
    public void calculateTotalPrice() {
        if (unitPrice != null && quantityOrdered != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantityOrdered));
        }
    }

    public Integer getQuantityPending() {
        return quantityOrdered - quantityReceived;
    }

    public boolean isFullyReceived() {
        return quantityReceived >= quantityOrdered;
    }

    public boolean isPartiallyReceived() {
        return quantityReceived > 0 && quantityReceived < quantityOrdered;
    }

    public void receiveQuantity(Integer quantity) {
        if (quantity > 0 && (quantityReceived + quantity) <= quantityOrdered) {
            this.quantityReceived += quantity;
        } else {
            throw new IllegalArgumentException(
                    "Invalid quantity to receive or exceeds ordered quantity");
        }
    }

    @Override
    public String toString() {
        return "OrderDetail{"
                + "id="
                + id
                + ", orderId="
                + (order != null ? order.getId() : null)
                + ", productId="
                + (product != null ? product.getId() : null)
                + ", quantityOrdered="
                + quantityOrdered
                + ", quantityReceived="
                + quantityReceived
                + ", unitPrice="
                + unitPrice
                + ", totalPrice="
                + totalPrice
                + '}';
    }
}
