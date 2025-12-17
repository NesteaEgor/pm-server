package ru.nesterov.pmserver.features.projects.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nesterov.pmserver.features.projects.members.repository.ProjectMemberRepository;
import ru.nesterov.pmserver.features.projects.repository.ProjectRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectAccessService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public boolean hasAccess(UUID userId, UUID projectId) {
        return projectRepository.existsByIdAndOwnerId(projectId, userId)
                || projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    public void requireAccess(UUID userId, UUID projectId) {
        // "не видно", что проект существует
        if (!hasAccess(userId, projectId)) {
            throw new IllegalArgumentException("Project not found");
        }
    }

    public void requireOwner(UUID userId, UUID projectId) {
        if (!projectRepository.existsByIdAndOwnerId(projectId, userId)) {
            throw new IllegalArgumentException("Project not found");
        }
    }
}
