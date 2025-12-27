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

    public boolean isOwner(UUID userId, UUID projectId) {
        return projectRepository.existsByIdAndOwnerId(projectId, userId);
    }

    public boolean hasAccess(UUID userId, UUID projectId) {
        return isOwner(userId, projectId)
                || projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    /** Проверка: targetUser состоит в проекте (owner или member) */
    public boolean isUserInProject(UUID targetUserId, UUID projectId) {
        return isOwner(targetUserId, projectId)
                || projectMemberRepository.existsByProjectIdAndUserId(projectId, targetUserId);
    }

    public void requireAccess(UUID userId, UUID projectId) {
        if (!hasAccess(userId, projectId)) {
            throw new IllegalArgumentException("Project not found");
        }
    }

    public void requireOwner(UUID userId, UUID projectId) {
        if (!isOwner(userId, projectId)) {
            throw new IllegalArgumentException("Project not found");
        }
    }
}
