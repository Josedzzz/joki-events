package com.uq.jokievents.utils;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data; // Generic field for additional data

    // Constructor
    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}