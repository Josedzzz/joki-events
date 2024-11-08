package com.uq.jokievents.exceptions.validation;

import com.uq.jokievents.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ControllerException {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();

        // Extract the validation errors from the exception object
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.add(error.getDefaultMessage())
        );

        String validationErrors = String.join(", ", errors);
        // Create a response with the parsed validation errors
        ApiResponse<List<String>> response = new ApiResponse<>(
                "Validation Failed",
                validationErrors,
                null // List<String> of error messages
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}

