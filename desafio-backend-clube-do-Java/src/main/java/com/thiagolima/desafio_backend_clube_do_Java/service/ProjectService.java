package com.thiagolima.desafio_backend_clube_do_Java.service;

import java.util.List;
import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.thiagolima.desafio_backend_clube_do_Java.dto.project.ProjectRequest;
import com.thiagolima.desafio_backend_clube_do_Java.dto.project.ProjectResponse;
import com.thiagolima.desafio_backend_clube_do_Java.enums.ProjectStatus;
import com.thiagolima.desafio_backend_clube_do_Java.exception.ProjectNotFoundException;
import com.thiagolima.desafio_backend_clube_do_Java.model.Project;
import com.thiagolima.desafio_backend_clube_do_Java.model.User;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.ProjectRepository;
import com.thiagolima.desafio_backend_clube_do_Java.utils.GetUserAuthentication;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {

        private final ProjectRepository projectRepository;

        public ProjectResponse createProject(ProjectRequest request) {
                User user = GetUserAuthentication.getUserAuthenticated();
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

        public void completedProject(Long id) {
                User user = GetUserAuthentication.getUserAuthenticated();
                Project project = projectRepository.findById(id)
                                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado"));

                if (project.getStatus() != ProjectStatus.IN_PROGRESS) {
                        throw new ProjectNotFoundException("O projeto não está em andamento");
                }

                if (!Objects.equals(project.getFreelancerId().getId(), user.getId())) {
                        throw new AccessDeniedException("Você não é o freelancer do projeto");
                }

                project.setStatus(ProjectStatus.COMPLETED);
                projectRepository.save(project);
        }

        public void finishedProject(Long id) {
                User user = GetUserAuthentication.getUserAuthenticated();
                Project project = projectRepository.findById(id)
                                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado"));

                if (project.getStatus() != ProjectStatus.COMPLETED) {
                        throw new ProjectNotFoundException("O projeto não está completo");
                }

                if (!Objects.equals(project.getClientId().getId(), user.getId())) {
                        throw new AccessDeniedException("Você não é o cliente do projeto");
                }

                project.setStatus(ProjectStatus.FINISHED);
                projectRepository.save(project);
        }

        public void deleteProject(Long id) {
                Project project = projectRepository.findById(id)
                                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado"));
                projectRepository.delete(project);
        }

}
