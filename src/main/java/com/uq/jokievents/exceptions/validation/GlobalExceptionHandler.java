package com.uq.jokievents.exceptions.validation;

import com.uq.jokievents.utils.ApiResponse;
import com.uq.jokievents.utils.ApiTokenResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();

        // Extract the validation errors from the exception object
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.add(error.getDefaultMessage())
        );

        String validationErrors = String.join(", ", errors);
        // Create a response with the parsed validation errors
        ApiResponse<List<String>> response = new ApiResponse<>("Validation Failed",validationErrors,null );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handle expired JWT specifically
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiTokenResponse<?>> handleExpiredJwtException(ExpiredJwtException e) {
        ApiTokenResponse<?> response = new ApiTokenResponse<>("Error", e.getMessage(), null, null);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // General handler for other JWT-related exceptions if needed
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiTokenResponse<?>> handleJwtException(JwtException e) {
        ApiTokenResponse<?> response = new ApiTokenResponse<>("Error", e.getMessage(), null, null);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // Catch-all for other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiTokenResponse<?>> handleGeneralException(Exception e) {
        ApiTokenResponse<?> response = new ApiTokenResponse<>("Error", e.getMessage(), null, null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

