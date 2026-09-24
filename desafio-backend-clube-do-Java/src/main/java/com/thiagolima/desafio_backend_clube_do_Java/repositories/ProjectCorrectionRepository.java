package com.thiagolima.desafio_backend_clube_do_Java.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagolima.desafio_backend_clube_do_Java.model.ProjectCorrection;

public interface ProjectCorrectionRepository extends JpaRepository<ProjectCorrection, Long> {

    List<ProjectCorrection> findByProjectId(Long projectId);
}
