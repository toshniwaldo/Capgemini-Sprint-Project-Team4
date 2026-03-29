package com.example.DemoCheck.exception;

import com.example.DemoCheck.handler.EmployeeEventHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<String> handleValidation(Exception ex) {
                return ResponseEntity.badRequest().body("Validation failed");
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<String> handleDBException(Exception ex) {
                return ResponseEntity
                                .badRequest()
                                .body("Invalid data: Required fields missing");
        }

        // Invalid ID format
        @ExceptionHandler(ConversionFailedException.class)
        public ResponseEntity<ErrorResponse> handleConversionFailed(
                        ConversionFailedException ex,
                        HttpServletRequest request) {

                ErrorResponse error = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Invalid ID format. ID must be a number.",
                                request.getRequestURI(),
                                LocalDateTime.now());

         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

        // Customer not found (valid ID but not present)
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(
                        ResourceNotFoundException ex,
                        HttpServletRequest request) {

                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Customer not found",
                                request.getRequestURI(),
                                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                400,
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse error = new ErrorResponse(
                400,
                message,
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDBError(
            Exception ex,
            HttpServletRequest request) {

        try {
            Integer id = EmployeeEventHandler.currentEmployeeId.get();

            String message = (id != null)
                    ? "Employee already exists with id: " + id
                    : "Duplicate employee";

            ErrorResponse error = new ErrorResponse(
                    400,
                    message,
                    request.getRequestURI(),
                    LocalDateTime.now()
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } finally {
            // ✅ CLEANUP HERE
            EmployeeEventHandler.currentEmployeeId.remove();
        }
    }


    //Generic fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
