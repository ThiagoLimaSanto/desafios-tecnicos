package com.thiagolima.desafio_backend_clube_do_Java.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagolima.desafio_backend_clube_do_Java.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}