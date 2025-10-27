package dev.kreaker.kinvex.dto.order;

/**
 * DTO de respuesta para los detalles de recepción de productos.
 */
public class OrderDetailReceiptResponse {

    private Long orderDetailId;
    private Long productId;
    private String productCode;
    private String productName;
    private Integer quantityOrdered;
    private Integer quantityPreviouslyReceived;
    private Integer quantityReceived;
    private Integer quantityTotalReceived;
    private Integer quantityPending;
    private boolean fullyReceived;

    // Default constructor
    public OrderDetailReceiptResponse() {
    }

    // Constructor with all fields
    public OrderDetailReceiptResponse(Long orderDetailId, Long productId, String productCode,
            String productName, Integer quantityOrdered,
            Integer quantityPreviouslyReceived, Integer quantityReceived,
            Integer quantityTotalReceived, Integer quantityPending,
            boolean fullyReceived) {
        this.orderDetailId = orderDetailId;
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.quantityOrdered = quantityOrdered;
        this.quantityPreviouslyReceived = quantityPreviouslyReceived;
        this.quantityReceived = quantityReceived;
        this.quantityTotalReceived = quantityTotalReceived;
        this.quantityPending = quantityPending;
        this.fullyReceived = fullyReceived;
    }

    // Getters and Setters
    public Long getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(Long orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

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

    public Integer getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(Integer quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public Integer getQuantityPreviouslyReceived() {
        return quantityPreviouslyReceived;
    }

    public void setQuantityPreviouslyReceived(Integer quantityPreviouslyReceived) {
        this.quantityPreviouslyReceived = quantityPreviouslyReceived;
    }

    public Integer getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(Integer quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public Integer getQuantityTotalReceived() {
        return quantityTotalReceived;
    }

    public void setQuantityTotalReceived(Integer quantityTotalReceived) {
        this.quantityTotalReceived = quantityTotalReceived;
    }

    public Integer getQuantityPending() {
        return quantityPending;
    }

    public void setQuantityPending(Integer quantityPending) {
        this.quantityPending = quantityPending;
    }

    public boolean isFullyReceived() {
        return fullyReceived;
    }

    public void setFullyReceived(boolean fullyReceived) {
        this.fullyReceived = fullyReceived;
    }

    @Override
    public String toString() {
        return "OrderDetailReceiptResponse{"
                + "orderDetailId=" + orderDetailId
                + ", productId=" + productId
                + ", productCode='" + productCode + '\''
                + ", productName='" + productName + '\''
                + ", quantityOrdered=" + quantityOrdered
                + ", quantityPreviouslyReceived=" + quantityPreviouslyReceived
                + ", quantityReceived=" + quantityReceived
                + ", quantityTotalReceived=" + quantityTotalReceived
                + ", quantityPending=" + quantityPending
                + ", fullyReceived=" + fullyReceived
                + '}';
    }
}
