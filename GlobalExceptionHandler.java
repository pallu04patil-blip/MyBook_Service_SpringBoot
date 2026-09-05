package com.example.bookservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// @RestControllerAdvice makes this a global, cross-controller handler:
// any exception thrown inside a @RestController anywhere in the app
// is caught here instead of turning into a raw 500 stack trace.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Triggered automatically when a @Valid-annotated @RequestBody fails
    // its bean-validation checks (e.g. blank title, blank author).
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation failed");
        body.put("details", fieldErrors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(BookNotFoundException ex) {
        return notFoundBody(ex.getMessage());
    }

    @ExceptionHandler(AuthorNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(AuthorNotFoundException ex) {
        return notFoundBody(ex.getMessage());
    }

    // Triggered by the Lending service when a book is already borrowed
    // (can't borrow it again) or already available (can't return it again).
    // 409 Conflict — the request is well-formed, but clashes with the book's
    // current state.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(IllegalStateException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    private ResponseEntity<Map<String, Object>> notFoundBody(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", message);

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
}
