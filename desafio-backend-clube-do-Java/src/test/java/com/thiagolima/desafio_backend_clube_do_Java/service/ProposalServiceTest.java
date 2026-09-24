package com.thiagolima.desafio_backend_clube_do_Java.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.thiagolima.desafio_backend_clube_do_Java.enums.*;
import com.thiagolima.desafio_backend_clube_do_Java.exception.ProjectExistFreelancerException;
import com.thiagolima.desafio_backend_clube_do_Java.model.*;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.*;

class ProposalServiceTest {
    private final ProposalRepository proposals = mock(ProposalRepository.class);
    private final ProjectRepository projects = mock(ProjectRepository.class);
    private final ProposalService service = new ProposalService(proposals, projects);
    private Project project;
    private Proposal proposal;
    private User freelancer;

    @BeforeEach
    void setup() {
        User client = user(1000L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(client, null, List.of()));
        project = new Project();
        ReflectionTestUtils.setField(project, "id", 10L);
        project.setClientId(user(1000L));
        project.setStatus(ProjectStatus.OPEN);
        freelancer = user(2000L);
        proposal = new Proposal();
        ReflectionTestUtils.setField(proposal, "id", 20L);
        proposal.setProject(project);
        proposal.setFreelancer(freelancer);
        when(projects.findById(10L)).thenReturn(Optional.of(project));
        when(proposals.findById(20L)).thenReturn(Optional.of(proposal));
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void acceptsProposalAndAssignsItsFreelancer() {
        service.accept(10L, 20L);
        assertEquals(ProposalStatus.ACCEPTED, proposal.getStatus());
        assertSame(freelancer, project.getFreelancerId());
        assertEquals(ProjectStatus.IN_PROGRESS, project.getStatus());
        verify(proposals).save(proposal);
        verify(projects).save(project);
    }

    @Test
    void rejectsProposalWithoutChangingProject() {
        service.reject(10L, 20L);
        assertEquals(ProposalStatus.REJECTED, proposal.getStatus());
        assertEquals(ProjectStatus.OPEN, project.getStatus());
        assertNull(project.getFreelancerId());
        verify(proposals).save(proposal);
        verify(projects, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void refusesProposalFromAnotherProject(boolean accept) {
        Project other = new Project();
        ReflectionTestUtils.setField(other, "id", 11L);
        proposal.setProject(other);
        assertThrows(AccessDeniedException.class, () -> decide(accept));
        verifyNoSaves();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void refusesAnotherClient(boolean accept) {
        project.setClientId(user(3000L));
        assertThrows(AccessDeniedException.class, () -> decide(accept));
        verifyNoSaves();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void refusesAlreadyDecidedProposals(boolean accept) {
        for (ProposalStatus status : List.of(ProposalStatus.ACCEPTED, ProposalStatus.REJECTED)) {
            proposal.setStatus(status);
            ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> decide(accept));
            assertEquals(409, error.getStatusCode().value());
        }
        verifyNoSaves();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void refusesClosedProject(boolean accept) {
        project.setStatus(ProjectStatus.IN_PROGRESS);
        assertThrows(ResponseStatusException.class, () -> decide(accept));
        verifyNoSaves();
    }

    @Test
    void doesNotReplaceExistingFreelancer() {
        User existing = user(4000L);
        project.setFreelancerId(existing);
        assertThrows(ProjectExistFreelancerException.class, () -> service.accept(10L, 20L));
        assertSame(existing, project.getFreelancerId());
        assertEquals(ProposalStatus.PENDING, proposal.getStatus());
        verifyNoSaves();
    }

    @Test
    void listsAllProposalsWithTheirStatus() {
        Proposal second = new Proposal();
        ReflectionTestUtils.setField(second, "id", 21L);
        second.setFreelancer(freelancer);
        second.setStatus(ProposalStatus.REJECTED);
        when(proposals.findByProjectId(10L)).thenReturn(List.of(proposal, second));
        var response = service.listByProject(10L);
        assertEquals(2, response.size());
        assertEquals(ProposalStatus.PENDING, response.get(0).status());
        assertEquals(ProposalStatus.REJECTED, response.get(1).status());
    }

    @Test
    void startsNegotiationWithoutAssigningFreelancer() {
        service.negotiate(10L, 20L);
        assertEquals(ProjectStatus.IN_NEGOCIATION, project.getStatus());
        assertEquals(ProposalStatus.IN_NEGOCIATION, proposal.getStatus());
        assertNull(project.getFreelancerId());
        verify(projects).save(project);
        verify(proposals).save(proposal);
    }

    @Test
    void acceptsNegotiatedProposal() {
        service.negotiate(10L, 20L);
        clearInvocations(projects, proposals);
        service.accept(10L, 20L);
        assertEquals(ProjectStatus.IN_PROGRESS, project.getStatus());
        assertEquals(ProposalStatus.ACCEPTED, proposal.getStatus());
        assertSame(freelancer, project.getFreelancerId());
        verify(projects).save(project);
        verify(proposals).save(proposal);
    }

    @Test
    void rejectingNegotiationReopensProjectAndAllowsAnotherNegotiation() {
        service.negotiate(10L, 20L);
        clearInvocations(projects, proposals);
        service.reject(10L, 20L);
        assertEquals(ProjectStatus.OPEN, project.getStatus());
        assertEquals(ProposalStatus.REJECTED, proposal.getStatus());
        assertNull(project.getFreelancerId());
        verify(projects).save(project);
        verify(proposals).save(proposal);

        Proposal second = new Proposal();
        second.setProject(project);
        second.setFreelancer(user(3000L));
        when(proposals.findById(21L)).thenReturn(Optional.of(second));
        service.negotiate(10L, 21L);
        assertEquals(ProjectStatus.IN_NEGOCIATION, project.getStatus());
        assertEquals(ProposalStatus.IN_NEGOCIATION, second.getStatus());
        assertEquals(ProposalStatus.REJECTED, proposal.getStatus());
    }

    @Test
    void cannotDecideOrNegotiateAnotherProposalDuringNegotiation() {
        service.negotiate(10L, 20L);
        Proposal second = new Proposal();
        second.setProject(project);
        when(proposals.findById(21L)).thenReturn(Optional.of(second));
        clearInvocations(projects, proposals);
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> service.accept(10L, 21L)).getStatusCode().value());
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> service.reject(10L, 21L)).getStatusCode().value());
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> service.negotiate(10L, 21L)).getStatusCode().value());
        assertEquals(ProposalStatus.PENDING, second.getStatus());
        assertEquals(ProposalStatus.IN_NEGOCIATION, proposal.getStatus());
        verifyNoSaves();
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class,
            names = "OPEN", mode = EnumSource.Mode.EXCLUDE)
    void negotiationRequiresOpenProject(ProjectStatus status) {
        project.setStatus(status);
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> service.negotiate(10L, 20L)).getStatusCode().value());
        assertEquals(status, project.getStatus());
        verifyNoSaves();
    }

    @ParameterizedTest
    @EnumSource(value = ProposalStatus.class,
            names = "PENDING", mode = EnumSource.Mode.EXCLUDE)
    void negotiationRequiresPendingProposal(ProposalStatus status) {
        proposal.setStatus(status);
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> service.negotiate(10L, 20L)).getStatusCode().value());
        verifyNoSaves();
    }

    @Test
    void negotiationRequiresOwner() {
        project.setClientId(user(3000L));
        assertThrows(AccessDeniedException.class, () -> service.negotiate(10L, 20L));
        verifyNoSaves();
    }

    @Test
    void negotiationRequiresProposalFromSameProject() {
        Project other = new Project();
        ReflectionTestUtils.setField(other, "id", 11L);
        proposal.setProject(other);
        assertThrows(AccessDeniedException.class, () -> service.negotiate(10L, 20L));
        verifyNoSaves();
    }

    @Test
    void negotiationDoesNotReplaceAssignedFreelancer() {
        project.setFreelancerId(freelancer);
        assertThrows(ProjectExistFreelancerException.class, () -> service.negotiate(10L, 20L));
        assertEquals(ProjectStatus.OPEN, project.getStatus());
        assertEquals(ProposalStatus.PENDING, proposal.getStatus());
        verifyNoSaves();
    }

    private void decide(boolean accept) {
        if (accept) service.accept(10L, 20L);
        else service.reject(10L, 20L);
    }

    private void verifyNoSaves() {
        verify(proposals, never()).save(any());
        verify(projects, never()).save(any());
    }

    private User user(Long id) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
