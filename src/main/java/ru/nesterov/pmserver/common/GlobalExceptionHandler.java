package ru.nesterov.pmserver.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        String msg = ex.getMessage() == null ? "Bad request" : ex.getMessage();

        HttpStatus status;
        if (msg.toLowerCase().contains("exists")) status = HttpStatus.CONFLICT;      // 409
        else if (msg.toLowerCase().contains("invalid credentials")) status = HttpStatus.UNAUTHORIZED; // 401
        else status = HttpStatus.BAD_REQUEST; // 400

        return ResponseEntity.status(status).body(Map.of("error", msg));
    }
}
