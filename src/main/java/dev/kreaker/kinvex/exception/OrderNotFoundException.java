package dev.kreaker.kinvex.exception;

/**
 * Excepción lanzada cuando no se encuentra una orden de compra.
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Orden de compra no encontrada con ID: " + orderId);
    }

    public OrderNotFoundException(String orderNumber) {
        super("Orden de compra no encontrada con número: " + orderNumber);
    }

    public OrderNotFoundException(String field, String value) {
        super("Orden de compra no encontrada con " + field + ": " + value);
    }

    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
