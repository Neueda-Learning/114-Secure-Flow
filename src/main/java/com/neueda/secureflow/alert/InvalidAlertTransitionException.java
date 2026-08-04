package com.neueda.secureflow.alert;

public class InvalidAlertTransitionException extends RuntimeException {
    public InvalidAlertTransitionException(String message) {
        super(message);
    }
}
