package com.thiagolima.desafio_backend_clube_do_Java.exception;

public class ProjectNotCompletedException extends RuntimeException {
    public ProjectNotCompletedException(String message) {
        super(message);
    }
}
