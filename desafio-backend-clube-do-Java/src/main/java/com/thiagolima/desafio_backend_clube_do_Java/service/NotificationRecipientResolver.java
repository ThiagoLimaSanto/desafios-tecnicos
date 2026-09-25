package com.thiagolima.desafio_backend_clube_do_Java.service;

import java.util.Objects;
import org.springframework.stereotype.Service;
import com.thiagolima.desafio_backend_clube_do_Java.enums.NotificationType;
import com.thiagolima.desafio_backend_clube_do_Java.enums.ProposalStatus;
import com.thiagolima.desafio_backend_clube_do_Java.model.Project;
import com.thiagolima.desafio_backend_clube_do_Java.repositories.ProposalRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationRecipientResolver {
    private final ProposalRepository proposals;

    public Long resolve(Project project, Long proposalId, NotificationType type) {
        if (type == NotificationType.PROPOSAL_NEGOTIATED) {
            if (proposalId == null) return null;
            return proposals.findById(proposalId)
                    .filter(p -> Objects.equals(p.getProject().getId(), project.getId()))
                    .map(p -> p.getFreelancer().getId()).orElse(null);
        }
        if (type == NotificationType.PROPOSAL_CREATED || type == NotificationType.PROJECT_COMPLETED) {
            return project.getClientId() == null ? null : project.getClientId().getId();
        }
        ProposalStatus status = type == NotificationType.PROPOSAL_REJECTED
                ? ProposalStatus.REJECTED : ProposalStatus.ACCEPTED;
        var recipients = proposals.findByProjectId(project.getId()).stream()
                .filter(p -> p.getStatus() == status && p.getFreelancer() != null)
                .map(p -> p.getFreelancer().getId()).filter(Objects::nonNull).distinct().toList();
        // A rejected proposal must never be attributed to the currently assigned freelancer.
        return recipients.size() == 1 ? recipients.getFirst() : null;
    }
}
