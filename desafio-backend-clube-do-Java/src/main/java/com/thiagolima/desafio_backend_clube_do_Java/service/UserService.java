package com.thiagolima.desafio_backend_clube_do_Java.service;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.thiagolima.desafio_backend_clube_do_Java.exception.AccountOwnerRequiredException;
import com.thiagolima.desafio_backend_clube_do_Java.exception.UserExistException;
import com.thiagolima.desafio_backend_clube_do_Java.exception.UserNotExistException;
import com.thiagolima.desafio_backend_clube_do_Java.exception.UserRoleRequiredException;
import com.thiagolima.desafio_backend_clube_do_Java.dto.user.CreateUserRequest;
import com.thiagolima.desafio_backend_clube_do_Java.dto.user.UpdateUserRequest;
import com.thiagolima.desafio_backend_clube_do_Java.dto.user.LoginUserRequest;
import com.thiagolima.desafio_backend_clube_do_Java.dto.user.UserResponse;
import com.thiagolima.desafio_backend_clube_do_Java.model.User;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public void create(CreateUserRequest request) {
        User user = new User();
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserExistException("Email já cadastrado");
        }

        if (request.role() == null) {
            throw new UserRoleRequiredException("Perfil é obrigatório");
        }
        user.setRole(request.role());
        user.setName(request.name());
        user.setDocument(request.document());
        user.setDocumentType(request.documentType());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        userRepository.save(user);
    }

    public UserResponse login(LoginUserRequest request) {
        var authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
        User user = (User) authentication.getPrincipal();
        return new UserResponse(jwtService.generateToken(user.getEmail(), user.getRole(), user.getName()));
    }

    public void update(UpdateUserRequest request, Long id) {
        requireOwnAccount(id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotExistException("Usuário não encontrado"));
        user.setName(request.name());
        user.setDocument(request.document());
        user.setDocumentType(request.documentType());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        userRepository.save(user);
    }

    public void delete(Long id) {
        requireOwnAccount(id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotExistException("Usuário não encontrado"));

        userRepository.delete(user);
    }

    private void requireOwnAccount(Long id) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof User principal)
                || id == null || !id.equals(principal.getId())) {
            throw new AccountOwnerRequiredException("Você só pode alterar ou excluir sua própria conta");
        }
    }
}
