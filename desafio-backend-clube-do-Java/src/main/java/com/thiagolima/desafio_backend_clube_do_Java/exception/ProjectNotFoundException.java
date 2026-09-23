package com.thiagolima.desafio_backend_clube_do_Java.exception;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(String message) {
        super(message);
    }
    
}
