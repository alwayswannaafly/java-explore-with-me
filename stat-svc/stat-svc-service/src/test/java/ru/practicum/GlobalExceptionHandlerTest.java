package ru.practicum;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.practicum.exception.DateValidationException;
import ru.practicum.exception.GlobalExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationException_ShouldReturnBadRequest() {
        // Given
        String message = "Start time must be before or equal to end time";
        DateValidationException exception = new DateValidationException(message);

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleValidationException(exception);

        // Then
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
        assertThat(response.getBody()).containsEntry("error", "Bad Request");
        assertThat(response.getBody()).containsEntry("message", message);
        assertThat(response.getBody()).containsKey("timestamp");
        assertThat(response.getBody().get("timestamp")).isInstanceOf(LocalDateTime.class);
    }
}