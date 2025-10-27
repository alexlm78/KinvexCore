package dev.kreaker.kinvex.exception;

/**
 * Excepción lanzada cuando se intenta realizar una operación que entra en conflicto con el estado
 * actual de una orden de compra.
 *
 * <p>Ejemplos: - Intentar recibir productos de una orden cancelada - Intentar cancelar una orden ya
 * completada - Intentar modificar una orden que ya está en proceso
 */
public class OrderStateConflictException extends RuntimeException {

    private final Long orderId;

    public OrderStateConflictException(String message) {
        super(message);
        this.orderId = null;
    }

    public OrderStateConflictException(Long orderId, String message) {
        super(message);
        this.orderId = orderId;
    }

    public OrderStateConflictException(String message, Throwable cause) {
        super(message, cause);
        this.orderId = null;
    }

    public OrderStateConflictException(Long orderId, String message, Throwable cause) {
        super(message, cause);
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }
}
