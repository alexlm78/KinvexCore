package dev.kreaker.kinvex.exception;

/** Excepción lanzada cuando no hay suficiente stock para realizar una operación. */
public class InsufficientStockException extends RuntimeException {

    private final Long productId;
    private final String productCode;
    private final Integer availableStock;
    private final Integer requestedQuantity;

    public InsufficientStockException(String message) {
        super(message);
        this.productId = null;
        this.productCode = null;
        this.availableStock = null;
        this.requestedQuantity = null;
    }

    public InsufficientStockException(
            Long productId, String productCode, Integer availableStock, Integer requestedQuantity) {
        super(
                String.format(
                        "Stock insuficiente para producto %s (ID: %d). Stock disponible: %d, cantidad solicitada: %d",
                        productCode, productId, availableStock, requestedQuantity));
        this.productId = productId;
        this.productCode = productCode;
        this.availableStock = availableStock;
        this.requestedQuantity = requestedQuantity;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }
}
