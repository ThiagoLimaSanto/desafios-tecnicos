package com.thiagolima.desafio_backend_clube_do_Java.service;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.thiagolima.desafio_backend_clube_do_Java.dto.project.ProposalRequest;
import com.thiagolima.desafio_backend_clube_do_Java.dto.project.ProposalResponse;
import com.thiagolima.desafio_backend_clube_do_Java.enums.ProjectStatus;
import com.thiagolima.desafio_backend_clube_do_Java.enums.ProposalStatus;
import com.thiagolima.desafio_backend_clube_do_Java.enums.UserRole;
import com.thiagolima.desafio_backend_clube_do_Java.exception.ProjectExistFreelancerException;
import com.thiagolima.desafio_backend_clube_do_Java.exception.ProjectNotFoundException;
import com.thiagolima.desafio_backend_clube_do_Java.model.Project;
import com.thiagolima.desafio_backend_clube_do_Java.model.Proposal;
import com.thiagolima.desafio_backend_clube_do_Java.model.User;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.ProjectRepository;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.ProposalRepository;
import com.thiagolima.desafio_backend_clube_do_Java.utils.GetUserAuthentication;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final ProjectRepository projectRepository;

    public void applyProject(Long id, ProposalRequest request) {
        User freelancer = GetUserAuthentication.getUserAuthenticated();

        if (freelancer.getRole() != UserRole.FREELANCER) {
            throw new AccessDeniedException(
                    "Somente freelancers podem enviar propostas");
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado"));

        Proposal proposal = new Proposal();
        proposal.setProject(project);
        proposal.setFreelancer(freelancer);
        proposal.setOfferedValue(request.offeredValue());
        proposal.setEstimatedDeliveryDate(request.estimatedDeliveryDate());

        proposalRepository.save(proposal);
    }

    public List<ProposalResponse> listByProject(Long projectId) {
        User client = GetUserAuthentication.getUserAuthenticated();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado"));

        if (!Objects.equals(client.getId(), project.getClientId().getId())) {
            throw new AccessDeniedException(
                    "Somente o cliente do projeto pode visualizar suas propostas");
        }

        return proposalRepository.findByProjectId(projectId)
                .stream()
                .map(proposal -> new ProposalResponse(proposal.getId(), proposal.getFreelancer().getId(),
                        proposal.getFreelancer().getName(), proposal.getOfferedValue(),
                        proposal.getEstimatedDeliveryDate(), proposal.getStatus()))
                .toList();
    }

    @Transactional
    public void accept(Long projectId, Long proposalId) {
        Proposal proposal = findProposalAndValidateOwner(projectId, proposalId);
        validateDecisionState(proposal);
        Project project = proposal.getProject();

        if (project.getFreelancerId() != null) {
            throw new ProjectExistFreelancerException(
                    "O projeto já possui um freelancer associado");
        }

        proposal.setStatus(ProposalStatus.ACCEPTED);
        project.setFreelancerId(proposal.getFreelancer());
        project.setStatus(ProjectStatus.IN_PROGRESS);

        proposalRepository.save(proposal);
        projectRepository.save(project);
    }

    @Transactional
    public void reject(Long projectId, Long proposalId) {
        Proposal proposal = findProposalAndValidateOwner(projectId, proposalId);
        validateDecisionState(proposal);
        Project project = proposal.getProject();
        if (proposal.getStatus() == ProposalStatus.IN_NEGOCIATION) {
            project.setStatus(ProjectStatus.OPEN);
            projectRepository.save(project);
        }
        proposal.setStatus(ProposalStatus.REJECTED);
        proposalRepository.save(proposal);
    }

    @Transactional
    public void negotiate(Long projectId, Long proposalId) {
        Proposal proposal = findProposalAndValidateOwner(projectId, proposalId);
        Project project = proposal.getProject();
        if (project.getStatus() != ProjectStatus.OPEN || proposal.getStatus() != ProposalStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A negociação exige um projeto aberto e uma proposta pendente");
        }
        if (project.getFreelancerId() != null) {
            throw new ProjectExistFreelancerException(
                    "O projeto já possui um freelancer associado");
        }

        project.setStatus(ProjectStatus.IN_NEGOCIATION);
        proposal.setStatus(ProposalStatus.IN_NEGOCIATION);
        projectRepository.save(project);
        proposalRepository.save(proposal);
    }

    private Proposal findProposalAndValidateOwner(Long projectId, Long proposalId) {
        User client = GetUserAuthentication.getUserAuthenticated();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado"));

        if (!Objects.equals(client.getId(), project.getClientId().getId())) {
            throw new AccessDeniedException(
                    "Somente o cliente do projeto pode decidir sobre suas propostas");
        }

        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Proposta não encontrada"));

        if (!Objects.equals(proposal.getProject().getId(), projectId)) {
            throw new AccessDeniedException("A proposta não pertence ao projeto informado");
        }

        return proposal;
    }

    private void validateDecisionState(Proposal proposal) {
        ProjectStatus projectStatus = proposal.getProject().getStatus();
        boolean pendingInOpenProject = projectStatus == ProjectStatus.OPEN
                && proposal.getStatus() == ProposalStatus.PENDING;
        boolean negotiating = projectStatus == ProjectStatus.IN_NEGOCIATION
                && proposal.getStatus() == ProposalStatus.IN_NEGOCIATION;

        if (!pendingInOpenProject && !negotiating) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O estado do projeto ou da proposta não permite essa decisão");
        }
    }
}
