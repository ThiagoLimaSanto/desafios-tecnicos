package com.thiagolima.desafio_backend_clube_do_Java.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thiagolima.desafio_backend_clube_do_Java.dto.project.ProjectRequest;
import com.thiagolima.desafio_backend_clube_do_Java.dto.project.ProjectResponse;
import com.thiagolima.desafio_backend_clube_do_Java.service.ProjectService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/create")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));

    }

    @GetMapping("/get")
    public ResponseEntity<List<ProjectResponse>> getProjects() {
        return ResponseEntity.ok(projectService.getProjects());

    }

    @PutMapping("/{projectId}/update")
    public ResponseEntity<Void> updateProject(@Valid @RequestBody ProjectRequest request,
            @PathVariable Long projectId) {
        projectService.updateProject(request, projectId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{projectId}/completed")
    public ResponseEntity<Void> completedProject(@PathVariable Long projectId) {
        projectService.completedProject(projectId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{projectId}/finished")
    public ResponseEntity<Void> finishedProject(@PathVariable Long projectId) {
        projectService.finishedProject(projectId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{projectId}/delete")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok().build();
    }

}
