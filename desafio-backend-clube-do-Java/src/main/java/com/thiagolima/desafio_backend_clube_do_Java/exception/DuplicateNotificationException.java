package com.thiagolima.desafio_backend_clube_do_Java.exception;

public class DuplicateNotificationException extends RuntimeException {
    public DuplicateNotificationException(String message) {
        super(message);
    }
}
