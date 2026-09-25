package com.thiagolima.desafio_backend_clube_do_Java.exception;

import io.jsonwebtoken.JwtException;

public class MissingTokenSubjectException extends JwtException {
    public MissingTokenSubjectException(String message) {
        super(message);
    }
}
