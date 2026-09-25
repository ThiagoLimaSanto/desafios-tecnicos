package com.thiagolima.desafio_backend_clube_do_Java.exception;

import org.springframework.security.access.AccessDeniedException;

public class AccountOwnerRequiredException extends AccessDeniedException {
    public AccountOwnerRequiredException(String message) {
        super(message);
    }
}
