package dev.kreaker.kinvex.exception;

/**
 * Excepción lanzada cuando se intenta realizar una operación inválida en una
 * orden.
 */
public class InvalidOrderOperationException extends RuntimeException {

    public InvalidOrderOperationException(String message) {
        super(message);
    }

    public InvalidOrderOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
