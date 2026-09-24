package com.thiagolima.desafio_backend_clube_do_Java.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thiagolima.desafio_backend_clube_do_Java.dto.project.CorrectionsRequest;
import com.thiagolima.desafio_backend_clube_do_Java.dto.project.ProjectCorrectionResponse;
import com.thiagolima.desafio_backend_clube_do_Java.service.ProjectCorrectionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/projects/{projectId}/corrections")
@RequiredArgsConstructor
public class ProjectCorrectionController {

    private final ProjectCorrectionService projectCorrectionService;

    @PostMapping
    public ResponseEntity<Void> corrections(@PathVariable Long projectId,
            @Valid @RequestBody CorrectionsRequest request) {
        projectCorrectionService.corrections(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<ProjectCorrectionResponse>> listCorrectionByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectCorrectionService.listCorrectionByProject(projectId));
    }
}
