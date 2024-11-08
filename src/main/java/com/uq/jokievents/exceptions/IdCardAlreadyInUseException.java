package com.uq.jokievents.exceptions;

public class IdCardAlreadyInUseException extends RuntimeException {
    public IdCardAlreadyInUseException(String message) {
        super(message);
    }
}
