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

    /** Maneja excepciones generales no capturadas. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {

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
