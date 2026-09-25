package com.thiagolima.desafio_backend_clube_do_Java.exception;

public class UserRoleRequiredException extends RuntimeException {
    public UserRoleRequiredException(String message) {
        super(message);
    }
}
