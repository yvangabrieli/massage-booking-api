package com.massage.booking.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 *
 * Catches ALL exceptions thrown anywhere in the app
 * Converts them to clean JSON responses for the frontend
 *
 * Without this:
 * → Frontend gets ugly HTML error pages
 *
 * With this:
 * → Frontend gets clean JSON: { "error": "...", "message": "..." }
 *
 * @RestControllerAdvice = applies to all controllers
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle our custom business exceptions
     * BusinessException, ResourceNotFoundException, etc.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex) {

        log.warn("Business exception: {}", ex.getMessage());

        return ResponseEntity
                .status(ex.getStatus())
                .body(new ErrorResponse(
                        ex.getStatus().name(),
                        ex.getMessage()
                ));
    }

    /**
     * Handle validation errors (@Valid annotation)
     * When request body fields fail validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        // Collect all field errors
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(field, message);
        });

        log.warn("Validation error: {}", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "VALIDATION_ERROR",
                        "Validation failed",
                        fieldErrors
                ));
    }

    /**
     * Handle unexpected exceptions
     * Catch-all for anything we didn't expect
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex) {

        log.error("Unexpected error: ", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "INTERNAL_ERROR",
                        "An unexpected error occurred"
                ));
    }

    // ── Error Response DTO (inner class) ──

    /**
     * Standard error response format
     * This is what frontend receives on every error
     */
    public record ErrorResponse(
            String error,
            String message,
            Map<String, String> fieldErrors,
            LocalDateTime timestamp
    ) {
        // Constructor for simple errors
        public ErrorResponse(String error, String message) {
            this(error, message, null, LocalDateTime.now());
        }

        // Constructor for validation errors with field details
        public ErrorResponse(String error, String message,
                             Map<String, String> fieldErrors) {
            this(error, message, fieldErrors, LocalDateTime.now());
        }
    }
}