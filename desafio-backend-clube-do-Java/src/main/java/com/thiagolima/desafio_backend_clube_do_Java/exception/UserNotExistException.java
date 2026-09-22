package com.thiagolima.desafio_backend_clube_do_Java.exception;

public class UserNotExistException extends RuntimeException {
    public UserNotExistException(String message) {
        super(message);
    }
    
}
