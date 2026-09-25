package com.thiagolima.desafio_backend_clube_do_Java.exception;

public class ProjectNotOpenException extends RuntimeException {
    public ProjectNotOpenException(String message) {
        super(message);
    }
}
