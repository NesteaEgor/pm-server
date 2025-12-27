package ru.nesterov.pmserver.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest req
    ) {
        String msg = ex.getMessage() == null ? "Bad request" : ex.getMessage();
        String low = msg.toLowerCase();

        HttpStatus status = HttpStatus.BAD_REQUEST;

        if (low.contains("unauthorized")) status = HttpStatus.UNAUTHORIZED; // 401
        else if (low.contains("access denied") || low.contains("forbidden")) status = HttpStatus.FORBIDDEN; // 403
        else if (low.contains("not found")) status = HttpStatus.NOT_FOUND; // 404
        else if (low.contains("already exists") || low.contains("exists")) status = HttpStatus.CONFLICT; // 409
        else if (low.contains("invalid credentials")) status = HttpStatus.UNAUTHORIZED; // 401

        return ResponseEntity.status(status).body(buildError(status, msg, req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest req
    ) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));

        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(buildError(status, msg, req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(
            Exception ex,
            HttpServletRequest req
    ) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(buildError(status, "Internal server error", req.getRequestURI()));
    }

    private Map<String, Object> buildError(HttpStatus status, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        return body;
    }
}
