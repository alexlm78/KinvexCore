package dev.kreaker.kinvex.exception;

/** Excepción lanzada cuando se intenta crear una orden con un número que ya existe. */
public class DuplicateOrderNumberException extends RuntimeException {

    public DuplicateOrderNumberException(String orderNumber) {
        super("Ya existe una orden de compra con el número: " + orderNumber);
    }

    public DuplicateOrderNumberException(String message, Throwable cause) {
        super(message, cause);
    }
}
