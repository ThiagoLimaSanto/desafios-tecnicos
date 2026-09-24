package com.thiagolima.desafio_backend_clube_do_Java.service;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagolima.desafio_backend_clube_do_Java.dto.project.CorrectionsRequest;
import com.thiagolima.desafio_backend_clube_do_Java.dto.project.ProjectCorrectionResponse;
import com.thiagolima.desafio_backend_clube_do_Java.enums.ProjectStatus;
import com.thiagolima.desafio_backend_clube_do_Java.exception.ProjectNotFoundException;
import com.thiagolima.desafio_backend_clube_do_Java.model.Project;
import com.thiagolima.desafio_backend_clube_do_Java.model.ProjectCorrection;
import com.thiagolima.desafio_backend_clube_do_Java.model.User;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.ProjectCorrectionRepository;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.ProjectRepository;
import com.thiagolima.desafio_backend_clube_do_Java.utils.GetUserAuthentication;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectCorrectionService {

    private final ProjectRepository projectRepository;
    private final ProjectCorrectionRepository projectCorrectionRepository;

    @Transactional
    public void corrections(Long id, CorrectionsRequest request) {
        User user = GetUserAuthentication.getUserAuthenticated();
        ProjectCorrection projectCorrection = new ProjectCorrection();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado"));
        if (!Objects.equals(project.getClientId().getId(), user.getId())) {
            throw new AccessDeniedException("Você não é o dono do projeto");
        }

        if (project.getStatus() != ProjectStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O projeto não está concluído");
        }

        projectCorrection.setProject(project);
        projectCorrection.setCorrection(request.correction());
        project.setStatus(ProjectStatus.IN_PROGRESS);
        projectRepository.save(project);
        projectCorrectionRepository.save(projectCorrection);
    }

    public List<ProjectCorrectionResponse> listCorrectionByProject(Long projectId) {
        User user = GetUserAuthentication.getUserAuthenticated();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado"));

        boolean isClient = Objects.equals(user.getId(), project.getClientId().getId());
        boolean isFreelancer = project.getFreelancerId() != null
                && Objects.equals(user.getId(), project.getFreelancerId().getId());

        if (!isClient && !isFreelancer) {
            throw new AccessDeniedException(
                    "Somente o cliente ou o freelancer do projeto pode visualizar as correções");
        }

        return projectCorrectionRepository.findByProjectId(projectId)
                .stream()
                .map(projectCorrection -> new ProjectCorrectionResponse(projectCorrection.getId(),
                        projectCorrection.getCorrection()))
                .toList();
    }
}
