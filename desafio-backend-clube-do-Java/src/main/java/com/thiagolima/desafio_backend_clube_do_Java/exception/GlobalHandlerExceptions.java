package com.thiagolima.desafio_backend_clube_do_Java.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.thiagolima.desafio_backend_clube_do_Java.error.ErrorResponse;

@RestControllerAdvice
public class GlobalHandlerExceptions {

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException error) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(new ErrorResponse(401, "Email ou senha inválidos"));
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException error) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new ErrorResponse(403, error.getMessage()));
        }

        @ExceptionHandler(UserExistException.class)
        public ResponseEntity<ErrorResponse> handleUserExistException(UserExistException e) {
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                e.getMessage());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(error);
        }

        @ExceptionHandler(UserNotExistException.class)
        public ResponseEntity<ErrorResponse> handleUserNotExistException(UserNotExistException e) {
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                e.getMessage());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(error);
        }

        @ExceptionHandler(ProjectNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleProjectNotFoundException(ProjectNotFoundException e) {
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                e.getMessage());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(error);
        }

        @ExceptionHandler(ProjectExistFreelancerException.class)
        public ResponseEntity<ErrorResponse> handleProjectExistFreelancerException(ProjectExistFreelancerException e) {
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                e.getMessage());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(error);
        }
}
