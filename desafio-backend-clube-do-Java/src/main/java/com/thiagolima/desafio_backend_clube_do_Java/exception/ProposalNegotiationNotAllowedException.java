package com.thiagolima.desafio_backend_clube_do_Java.exception;

public class ProposalNegotiationNotAllowedException extends RuntimeException {
    public ProposalNegotiationNotAllowedException(String message) {
        super(message);
    }
}
