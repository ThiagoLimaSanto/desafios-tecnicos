package com.thiagolima.desafio_backend_clube_do_Java.exception;

public class EventPublishRejectedException extends RuntimeException {
    public EventPublishRejectedException(String message) {
        super(message);
    }
}
