package com.thiagolima.desafio_backend_clube_do_Java.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thiagolima.desafio_backend_clube_do_Java.dto.project.ProposalRequest;
import com.thiagolima.desafio_backend_clube_do_Java.dto.project.ProposalResponse;
import com.thiagolima.desafio_backend_clube_do_Java.service.ProposalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/projects/{projectId}/proposals")
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;

    @PostMapping("/apply")
    public ResponseEntity<Void> applyProject(@PathVariable Long projectId,
            @Valid @RequestBody ProposalRequest request) {
        proposalService.applyProject(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<ProposalResponse>> listByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(proposalService.listByProject(projectId));
    }

    @PutMapping("/{proposalId}/accept")
    public ResponseEntity<Void> accept(@PathVariable Long projectId, @PathVariable Long proposalId) {
        proposalService.accept(projectId, proposalId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{proposalId}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long projectId, @PathVariable Long proposalId) {
        proposalService.reject(projectId, proposalId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{proposalId}/negotiate")
    public ResponseEntity<Void> negotiate(@PathVariable Long projectId, @PathVariable Long proposalId) {
        proposalService.negotiate(projectId, proposalId);
        return ResponseEntity.ok().build();
    }
}
