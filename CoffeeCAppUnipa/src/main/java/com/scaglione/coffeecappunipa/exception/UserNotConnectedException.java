package com.scaglione.coffeecappunipa.exception;

public class UserNotConnectedException extends RuntimeException {
    public UserNotConnectedException(String message) {
        super(message);
    }
}