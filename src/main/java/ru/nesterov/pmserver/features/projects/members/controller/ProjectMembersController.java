package ru.nesterov.pmserver.features.projects.members.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.nesterov.pmserver.features.projects.members.dto.AddProjectMemberRequest;
import ru.nesterov.pmserver.features.projects.members.dto.ProjectMemberDto;
import ru.nesterov.pmserver.features.projects.members.service.ProjectMemberService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMembersController {

    private final ProjectMemberService service;

    @GetMapping
    public List<ProjectMemberDto> list(Authentication auth, @PathVariable UUID projectId) {
        UUID userId = (UUID) auth.getPrincipal();
        return service.list(userId, projectId);
    }

    @PostMapping
    public ProjectMemberDto add(Authentication auth,
                                @PathVariable UUID projectId,
                                @Valid @RequestBody AddProjectMemberRequest req) {
        UUID ownerId = (UUID) auth.getPrincipal();
        return service.addMemberByEmail(ownerId, projectId, req.getEmail().trim().toLowerCase());
    }

    @DeleteMapping("/{memberUserId}")
    public void remove(Authentication auth,
                       @PathVariable UUID projectId,
                       @PathVariable UUID memberUserId) {
        UUID ownerId = (UUID) auth.getPrincipal();
        service.removeMember(ownerId, projectId, memberUserId);
    }
}
