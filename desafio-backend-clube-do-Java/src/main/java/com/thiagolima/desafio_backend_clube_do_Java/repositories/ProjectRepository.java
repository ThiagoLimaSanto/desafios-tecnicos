package com.thiagolima.desafio_backend_clube_do_Java.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagolima.desafio_backend_clube_do_Java.model.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

}
