package com.thiagolima.desafio_backend_clube_do_Java.service;

import org.springframework.stereotype.Service;

import com.thiagolima.desafio_backend_clube_do_Java.dto.user.CreateUserRequest;
import com.thiagolima.desafio_backend_clube_do_Java.dto.user.LoginUserRequest;
import com.thiagolima.desafio_backend_clube_do_Java.dto.user.UserResponse;
import com.thiagolima.desafio_backend_clube_do_Java.exception.UserExistException;
import com.thiagolima.desafio_backend_clube_do_Java.exception.UserNotExistException;
import com.thiagolima.desafio_backend_clube_do_Java.model.User;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void create(CreateUserRequest request) {
        User user = new User();
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserExistException("Email já cadastrado");
        }

        user.setName(request.name());
        user.setDocument(request.document());
        user.setDocumentType(request.documentType());
        user.setEmail(request.email());
        user.setPassword(request.password());

        userRepository.save(user);
    }

    public UserResponse login(LoginUserRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotExistException("Email ou senha inválidos"));
        return new UserResponse(user.getId().toString());
    }

    public void update(CreateUserRequest request, Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotExistException("Usuário não encontrado"));
        user.setName(request.name());
        user.setDocument(request.document());
        user.setDocumentType(request.documentType());
        user.setEmail(request.email());
        user.setPassword(request.password());

        userRepository.save(user);
    }

    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotExistException("Usuário não encontrado"));

        userRepository.delete(user);
    }
}
