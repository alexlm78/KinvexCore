package dev.kreaker.kinvex.exception;

/** Excepción lanzada cuando se intenta crear un producto con un código que ya existe. */
public class DuplicateProductCodeException extends RuntimeException {

    private final String productCode;

    public DuplicateProductCodeException(String productCode) {
        super("Ya existe un producto con el código: " + productCode);
        this.productCode = productCode;
    }

    public String getProductCode() {
        return productCode;
    }
}
