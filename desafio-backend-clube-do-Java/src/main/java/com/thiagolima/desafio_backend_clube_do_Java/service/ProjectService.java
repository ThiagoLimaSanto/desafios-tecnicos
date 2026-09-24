package com.thiagolima.desafio_backend_clube_do_Java.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.thiagolima.desafio_backend_clube_do_Java.dto.project.ProjectRequest;
import com.thiagolima.desafio_backend_clube_do_Java.dto.project.ProjectResponse;
import com.thiagolima.desafio_backend_clube_do_Java.enums.ProjectStatus;
import com.thiagolima.desafio_backend_clube_do_Java.exception.ProjectNotFoundException;
import com.thiagolima.desafio_backend_clube_do_Java.model.Project;
import com.thiagolima.desafio_backend_clube_do_Java.model.User;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {

        private final ProjectRepository projectRepository;

        public ProjectResponse createProject(ProjectRequest request) {
                User user = getUserAuthenticated();
                Project project = new Project();
                project.setTitle(request.title());
                project.setClientId(user);
                project.setFreelancerId(null);
                project.setDescription(request.description());
                project.setDeadline(request.deadline());
                project.setEstimatedBudget(request.estimatedBudget());
                project.setStatus(ProjectStatus.OPEN);
                projectRepository.save(project);
                return new ProjectResponse(project.getId(), project.getTitle(), project.getDescription(),
                                project.getDeadline(),
                                project.getEstimatedBudget());
        }

        public ProjectResponse getProjectById(Long id) {
                Project project = projectRepository.findById(id)
                                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado"));
                return new ProjectResponse(project.getId(), project.getTitle(), project.getDescription(),
                                project.getDeadline(),
                                project.getEstimatedBudget());
        }

        public List<ProjectResponse> getProjects() {
                return projectRepository.findAll().stream()
                                .map(project -> new ProjectResponse(project.getId(), project.getTitle(),
                                                project.getDescription(),
                                                project.getDeadline(),
                                                project.getEstimatedBudget()))
                                .toList();
        }

        public void updateProject(ProjectRequest request, Long id) {
                Project project = projectRepository.findById(id)
                                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado"));
                project.setTitle(request.title());
                project.setDescription(request.description());
                project.setDeadline(request.deadline());
                project.setEstimatedBudget(request.estimatedBudget());
                project.setStatus(ProjectStatus.OPEN);
                projectRepository.save(project);
        }

        public void deleteProject(Long id) {
                Project project = projectRepository.findById(id)
                                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado"));
                projectRepository.delete(project);
        }

        private User getUserAuthenticated() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                return (User) authentication.getPrincipal();
        }
}
