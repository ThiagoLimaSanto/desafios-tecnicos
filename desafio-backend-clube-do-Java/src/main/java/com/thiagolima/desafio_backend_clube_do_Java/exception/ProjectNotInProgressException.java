package com.thiagolima.desafio_backend_clube_do_Java.exception;

public class ProjectNotInProgressException extends RuntimeException {
    public ProjectNotInProgressException(String message) {
        super(message);
    }
}
