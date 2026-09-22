package com.thiagolima.desafio_backend_clube_do_Java.model;


import com.thiagolima.desafio_backend_clube_do_Java.enums.DocumentType;
import com.thiagolima.desafio_backend_clube_do_Java.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    private String name;
    private String document;

    @Column(name = "document_type")
    private DocumentType documentType;

    private String email;

    private String password;

    private UserRole role;
}
