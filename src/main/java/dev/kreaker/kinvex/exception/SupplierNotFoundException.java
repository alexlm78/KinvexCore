package dev.kreaker.kinvex.exception;

/** Excepción lanzada cuando no se encuentra un proveedor. */
public class SupplierNotFoundException extends RuntimeException {

    public SupplierNotFoundException(Long supplierId) {
        super("Proveedor no encontrado con ID: " + supplierId);
    }

    public SupplierNotFoundException(String supplierName) {
        super("Proveedor no encontrado con nombre: " + supplierName);
    }

    public SupplierNotFoundException(String field, String value) {
        super("Proveedor no encontrado con " + field + ": " + value);
    }

    public SupplierNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
