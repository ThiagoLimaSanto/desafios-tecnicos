package com.thiagolima.desafio_backend_clube_do_Java.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thiagolima.desafio_backend_clube_do_Java.dto.user.CreateUserRequest;
import com.thiagolima.desafio_backend_clube_do_Java.dto.user.UpdateUserRequest;
import com.thiagolima.desafio_backend_clube_do_Java.dto.user.LoginUserRequest;
import com.thiagolima.desafio_backend_clube_do_Java.dto.user.UserResponse;
import com.thiagolima.desafio_backend_clube_do_Java.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Void> create(@Valid @RequestBody CreateUserRequest request) {
        userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginUserRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Void> update(@Valid @RequestBody UpdateUserRequest request, @PathVariable Long id) {
        userService.update(request, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok().build();
    }
}
