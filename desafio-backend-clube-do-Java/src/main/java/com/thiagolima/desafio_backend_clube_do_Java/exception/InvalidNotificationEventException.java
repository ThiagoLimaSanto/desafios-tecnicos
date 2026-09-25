package com.thiagolima.desafio_backend_clube_do_Java.exception;

public class InvalidNotificationEventException extends RuntimeException {
    public InvalidNotificationEventException(String message) {
        super(message);
    }
}
