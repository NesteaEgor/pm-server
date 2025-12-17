package ru.nesterov.pmserver.features.projects.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.pmserver.features.projects.dto.CreateProjectRequest;
import ru.nesterov.pmserver.features.projects.dto.ProjectDto;
import ru.nesterov.pmserver.features.projects.entity.ProjectEntity;
import ru.nesterov.pmserver.features.projects.repository.ProjectRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectAccessService accessService;

    public ProjectDto create(UUID ownerId, CreateProjectRequest req) {
        ProjectEntity project = ProjectEntity.builder()
                .ownerId(ownerId)
                .name(req.getName().trim())
                .description(req.getDescription() == null ? null : req.getDescription().trim())
                .createdAt(Instant.now())
                .build();

        project = projectRepository.save(project);
        return toDto(project);
    }

    public List<ProjectDto> list(UUID userId) {
        return projectRepository.findAccessibleByUser(userId)
                .stream()
                .map(ProjectService::toDto)
                .toList();
    }

    public ProjectDto get(UUID userId, UUID projectId) {
        accessService.requireAccess(userId, projectId);

        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        return toDto(project);
    }


    public static ProjectDto toDto(ProjectEntity p) {
        return new ProjectDto(p.getId(), p.getName(), p.getDescription(), p.getCreatedAt());
    }

    @Transactional
    public void delete(UUID ownerId, UUID projectId) {
        var p = projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        projectRepository.delete(p);
    }

}
