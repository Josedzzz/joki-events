package com.uq.jokievents.exceptions;

public class ClientAlreadyActiveException extends RuntimeException {
    public ClientAlreadyActiveException(String message) {
        super(message);
    }
}
