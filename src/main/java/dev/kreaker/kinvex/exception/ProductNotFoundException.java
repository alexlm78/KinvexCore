package dev.kreaker.kinvex.exception;

/** Excepción lanzada cuando no se encuentra un producto en el sistema. */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductNotFoundException(Long productId) {
        super("Producto no encontrado con ID: " + productId);
    }

    public ProductNotFoundException(String field, String value) {
        super("Producto no encontrado con " + field + ": " + value);
    }
}
