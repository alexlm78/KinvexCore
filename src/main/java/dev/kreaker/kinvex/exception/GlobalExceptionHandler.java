package dev.kreaker.kinvex.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Manejador global de excepciones para la aplicación. Captura y maneja diferentes tipos de
 * excepciones de manera centralizada.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Maneja excepciones de autenticación. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {

        logger.warn("Error de autenticación: {}", ex.getMessage());

        ErrorResponse errorResponse =
                new ErrorResponse(
                        "AUTHENTICATION_ERROR",
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getDescription(false));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /** Maneja excepciones de token inválido. */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(
            InvalidTokenException ex, WebRequest request) {

        logger.warn("Token inválido: {}", ex.getMessage());

        ErrorResponse errorResponse =
                new ErrorResponse(
                        "INVALID_TOKEN",
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getDescription(false));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /** Maneja excepciones de producto no encontrado. */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(
            ProductNotFoundException ex, WebRequest request) {

        logger.warn("Producto no encontrado: {}", ex.getMessage());

        ErrorResponse errorResponse =
                new ErrorResponse(
                        "PRODUCT_NOT_FOUND",
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getDescription(false));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /** Maneja excepciones de stock insuficiente. */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(
            InsufficientStockException ex, WebRequest request) {

        logger.warn("Stock insuficiente: {}", ex.getMessage());

        ErrorResponse errorResponse =
                new ErrorResponse(
                        "INSUFFICIENT_STOCK",
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getDescription(false));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /** Maneja excepciones de código de producto duplicado. */
    @ExceptionHandler(DuplicateProductCodeException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateProductCodeException(
            DuplicateProductCodeException ex, WebRequest request) {

        logger.warn("Código de producto duplicado: {}", ex.getMessage());

        ErrorResponse errorResponse =
                new ErrorResponse(
                        "DUPLICATE_PRODUCT_CODE",
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getDescription(false));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /** Maneja excepciones de orden no encontrada. */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFoundException(
            OrderNotFoundException ex, WebRequest request) {

        logger.warn("Orden no encontrada: {}", ex.getMessage());

        ErrorResponse errorResponse =
                new ErrorResponse(
                        "ORDER_NOT_FOUND",
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getDescription(false));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /** Maneja excepciones de proveedor no encontrado. */
    @ExceptionHandler(SupplierNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSupplierNotFoundException(
            SupplierNotFoundException ex, WebRequest request) {

        logger.warn("Proveedor no encontrado: {}", ex.getMessage());

        ErrorResponse errorResponse =
                new ErrorResponse(
                        "SUPPLIER_NOT_FOUND",
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getDescription(false));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /** Maneja excepciones de número de orden duplicado. */
    @ExceptionHandler(DuplicateOrderNumberException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateOrderNumberException(
            DuplicateOrderNumberException ex, WebRequest request) {

        logger.warn("Número de orden duplicado: {}", ex.getMessage());

        ErrorResponse errorResponse =
                new ErrorResponse(
                        "DUPLICATE_ORDER_NUMBER",
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getDescription(false));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /** Maneja excepciones de operación inválida en orden. */
    @ExceptionHandler(InvalidOrderOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderOperationException(
            InvalidOrderOperationException ex, WebRequest request) {

        logger.warn("Operación inválida en orden: {}", ex.getMessage());

        ErrorResponse errorResponse =
                new ErrorResponse(
                        "INVALID_ORDER_OPERATION",
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getDescription(false));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /** Maneja excepciones de conflicto de estado en orden. */
    @ExceptionHandler(OrderStateConflictException.class)
    public ResponseEntity<ErrorResponse> handleOrderStateConflictException(
            OrderStateConflictException ex, WebRequest request) {

        logger.warn("Conflicto de estado en orden: {}", ex.getMessage());

        ErrorResponse errorResponse =
                new ErrorResponse(
                        "ORDER_STATE_CONFLICT",
                        ex.getMessage(),
                        LocalDateTime.now(),
                        request.getDescription(false));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /** Maneja errores de validación de datos de entrada. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        logger.warn("Error de validación: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(
                        (error) -> {
                            String fieldName = ((FieldError) error).getField();
                            String errorMessage = error.getDefaultMessage();
                            errors.put(fieldName, errorMessage);
                        });

        ValidationErrorResponse errorResponse =
                new ValidationErrorResponse(
                        "VALIDATION_ERROR",
                        "Datos de entrada inválidos",
                        LocalDateTime.now(),
                        request.getDescription(false),
                        errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja excepciones generales no capturadas. Excluye excepciones de autorización para que
     * Spring Security las maneje.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {

        // Permitir que Spring Security maneje las excepciones de autorización
        if (ex.getClass().getSimpleName().contains("Authorization")) {
            throw new RuntimeException(ex);
        }

        logger.error("Error interno del servidor: ", ex);

        ErrorResponse errorResponse =
                new ErrorResponse(
                        "INTERNAL_SERVER_ERROR",
                        "Error interno del servidor",
                        LocalDateTime.now(),
                        request.getDescription(false));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /** Respuesta estándar de error. */
    public static class ErrorResponse {

        private String code;
        private String message;
        private LocalDateTime timestamp;
        private String path;

        public ErrorResponse(String code, String message, LocalDateTime timestamp, String path) {
            this.code = code;
            this.message = message;
            this.timestamp = timestamp;
            this.path = path;
        }

        // Getters and Setters
        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    /** Respuesta de error para validaciones. */
    public static class ValidationErrorResponse extends ErrorResponse {

        private Map<String, String> fieldErrors;

        public ValidationErrorResponse(
                String code,
                String message,
                LocalDateTime timestamp,
                String path,
                Map<String, String> fieldErrors) {
            super(code, message, timestamp, path);
            this.fieldErrors = fieldErrors;
        }

        public Map<String, String> getFieldErrors() {
            return fieldErrors;
        }

        public void setFieldErrors(Map<String, String> fieldErrors) {
            this.fieldErrors = fieldErrors;
        }
    }
}
