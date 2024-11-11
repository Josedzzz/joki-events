package com.uq.jokievents.exceptions;

public class LogicException extends RuntimeException {
    public LogicException(String message, Throwable cause) {
        super(message, cause);
    }
    public LogicException(String message) {
        super(message);
    }
}
