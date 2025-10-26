package dev.kreaker.kinvex.exception;

/**
 * Excepción personalizada para errores de autenticación. Se lanza cuando las credenciales son
 * inválidas o el usuario no está autorizado.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
