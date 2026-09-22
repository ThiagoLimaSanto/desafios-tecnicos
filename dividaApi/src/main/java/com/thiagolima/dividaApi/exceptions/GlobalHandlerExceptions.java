package com.thiagolima.dividaApi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.thiagolima.dividaApi.error.ErrorResponse;

@RestControllerAdvice
public class GlobalHandlerExceptions {

    @ExceptionHandler(DividaNaoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleDividaNaoEncontradaException(DividaNaoEncontradaException e) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }
}
