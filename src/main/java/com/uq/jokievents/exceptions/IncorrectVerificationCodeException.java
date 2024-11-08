package com.uq.jokievents.exceptions;

public class IncorrectVerificationCodeException extends RuntimeException {
    public IncorrectVerificationCodeException(String message) {
        super(message);
    }
}
