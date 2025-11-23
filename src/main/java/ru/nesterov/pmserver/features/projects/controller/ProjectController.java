package ru.nesterov.pmserver.features.projects.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.nesterov.pmserver.features.projects.dto.CreateProjectRequest;
import ru.nesterov.pmserver.features.projects.dto.ProjectDto;
import ru.nesterov.pmserver.features.projects.service.ProjectService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ProjectDto create(Authentication auth, @Valid @RequestBody CreateProjectRequest req) {
        UUID userId = (UUID) auth.getPrincipal();
        return projectService.create(userId, req);
    }

    @GetMapping
    public List<ProjectDto> list(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return projectService.list(userId);
    }

    @GetMapping("/{id}")
    public ProjectDto get(Authentication auth, @PathVariable UUID id) {
        UUID userId = (UUID) auth.getPrincipal();
        return projectService.get(userId, id);
    }

    @DeleteMapping("/{id}")
    public void delete(Authentication auth, @PathVariable UUID id) {
        UUID userId = (UUID) auth.getPrincipal();
        projectService.delete(userId, id);
    }

}
