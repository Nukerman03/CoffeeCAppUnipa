package com.scaglione.coffeecappunipa.exception;

public class MachineUnavailableException extends RuntimeException {
    public MachineUnavailableException(String message) {
        super(message);
    }
}