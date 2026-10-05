package com.scaglione.coffeecappunipa.exception;

public class MachineNotFoundException extends RuntimeException {
    public MachineNotFoundException(String message) {
        super(message);
    }
}