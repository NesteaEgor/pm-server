package ru.nesterov.pmserver.features.projects.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    public List<ProjectDto> list(UUID ownerId) {
        return projectRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId)
                .stream()
                .map(ProjectService::toDto)
                .toList();
    }

    public ProjectDto get(UUID ownerId, UUID projectId) {
        ProjectEntity project = projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        return toDto(project);
    }

    public static ProjectDto toDto(ProjectEntity p) {
        return new ProjectDto(p.getId(), p.getName(), p.getDescription(), p.getCreatedAt());
    }
}
