package com.uq.jokievents.exceptions;

import com.uq.jokievents.utils.ApiResponse;
import org.checkerframework.checker.units.qual.A;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ControllerException {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // Extract the validation errors from the exception object
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        // Create a response with the parsed validation errors
        ApiResponse<Map<String, String>> response = new ApiResponse<>(
                "Validation Failed", errors.toString(), null);
        // {username=Username cannot be null}
        System.out.println(response);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


}
