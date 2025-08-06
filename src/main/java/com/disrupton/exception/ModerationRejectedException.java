package com.disrupton.exception;

public class ModerationRejectedException extends RuntimeException {
    public ModerationRejectedException(String mensaje) {
        super(mensaje);
    }
}
