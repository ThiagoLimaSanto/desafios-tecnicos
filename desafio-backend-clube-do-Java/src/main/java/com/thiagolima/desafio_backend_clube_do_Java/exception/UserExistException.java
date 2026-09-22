package com.thiagolima.desafio_backend_clube_do_Java.exception;

public class UserExistException extends RuntimeException {
    public UserExistException(String message) {
        super(message);
    }
}
