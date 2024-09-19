package com.uq.jokievents.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiTokenResponse<T> {
    private String status;
    private String message;
    private T data; // Generic field for additional data
    private T token; // For tokens
}