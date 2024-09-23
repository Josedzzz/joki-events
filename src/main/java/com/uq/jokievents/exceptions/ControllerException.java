package com.uq.jokievents.exceptions;

import com.uq.jokievents.utils.ApiResponse;
import org.checkerframework.checker.units.qual.A;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerException {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> validateException(MethodArgumentNotValidException ex) {
        return ResponseEntity.ok(new ApiResponse<>(ex.getStatusCode().toString(), ex.getMessage(), null));
    }

}
