package dev.kreaker.kinvex.exception;

/**
 * Excepción personalizada para tokens JWT inválidos. Se lanza cuando un token es malformado,
 * expirado o no válido.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
