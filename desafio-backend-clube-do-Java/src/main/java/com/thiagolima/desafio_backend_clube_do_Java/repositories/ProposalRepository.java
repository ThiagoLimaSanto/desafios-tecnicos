package com.thiagolima.desafio_backend_clube_do_Java.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagolima.desafio_backend_clube_do_Java.model.Proposal;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    List<Proposal> findByProjectId(Long projectId);
}