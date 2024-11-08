package com.uq.jokievents.exceptions;

public class EmptyShoppingCartException extends RuntimeException
{
    public EmptyShoppingCartException(String message) {
        super(message);
    }
}
