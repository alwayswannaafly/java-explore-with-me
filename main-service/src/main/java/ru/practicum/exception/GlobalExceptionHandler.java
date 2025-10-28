package ru.practicum.exception;

import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        Map<String, Object> body = Map.of(
                "status", "CONFLICT",
                "reason", "Integrity constraint has been violated.",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now().format(FORMATTER)
        );
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        Map<String, Object> body = Map.of(
                "status", "NOT_FOUND",
                "reason", "The required object was not found.",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now().format(FORMATTER)
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex) {
        Map<String, Object> body = Map.of(
                "status", "FORBIDDEN",
                "reason", "For the requested operation the conditions are not met.",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now().format(FORMATTER)
        );
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(ValidationException ex) {
        Map<String, Object> body = Map.of(
                "status", "BAD_REQUEST",
                "reason", "Incorrectly made request.",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now().format(FORMATTER)
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}