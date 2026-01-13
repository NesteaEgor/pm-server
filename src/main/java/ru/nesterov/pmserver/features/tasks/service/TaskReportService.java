package ru.nesterov.pmserver.features.tasks.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nesterov.pmserver.features.projects.members.service.ProjectMemberService;
import ru.nesterov.pmserver.features.projects.service.ProjectAccessService;
import ru.nesterov.pmserver.features.tasks.dto.TaskReportRowDto;
import ru.nesterov.pmserver.features.tasks.entity.TaskEntity;
import ru.nesterov.pmserver.features.tasks.repository.TaskRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskReportService {

    private final TaskRepository taskRepository;
    private final ProjectAccessService accessService;

    private final ProjectMemberService projectMemberService;

    public List<TaskReportRowDto> buildReport(
            UUID userId,
            UUID projectId,
            String role,
            String status
    ) {
        accessService.requireAccess(userId, projectId);

        // берём всех участников проекта (включая виртуального OWNER) -> карта userId -> displayName
        var members = projectMemberService.list(userId, projectId);
        Map<UUID, String> nameById = members.stream()
                .filter(m -> m.getUserId() != null)
                .collect(Collectors.toMap(
                        m -> m.getUserId(),
                        m -> {
                            String dn = (m.getDisplayName() == null ? "" : m.getDisplayName().trim());
                            if (!dn.isEmpty()) return dn;
                            String em = (m.getEmail() == null ? "" : m.getEmail().trim());
                            if (!em.isEmpty()) return em;
                            return m.getUserId().toString();
                        },
                        (a, b) -> a
                ));

        List<TaskEntity> tasks = taskRepository.findByProjectIdOrderByCreatedAtDesc(projectId);

        return tasks.stream()
                .filter(t -> filterByRole(t, userId, role))
                .filter(t -> filterByStatus(t, status))
                .map(t -> toRow(t, nameById))
                .toList();
    }

    private boolean filterByRole(TaskEntity t, UUID userId, String role) {
        if (role == null || role.isBlank()) return true;

        return switch (role.toLowerCase()) {
            case "creator" -> userId.equals(t.getCreatorId());
            case "assignee" -> t.getAssigneeId() != null && userId.equals(t.getAssigneeId());
            default -> true;
        };
    }

    private boolean filterByStatus(TaskEntity t, String status) {
        if (status == null || status.isBlank()) return true;
        return t.getStatus() != null && t.getStatus().equalsIgnoreCase(status.trim());
    }

    private static TaskReportRowDto toRow(TaskEntity t, Map<UUID, String> nameById) {
        Long days = null;

        if (t.getDeadline() != null && t.getStatus() != null && !"DONE".equalsIgnoreCase(t.getStatus())) {
            days = ChronoUnit.DAYS.between(Instant.now(), t.getDeadline());
        }

        String creatorName = safeName(nameById, t.getCreatorId());
        String assigneeName = t.getAssigneeId() == null ? "-" : safeName(nameById, t.getAssigneeId());

        return new TaskReportRowDto(
                t.getId(),
                t.getTitle(),
                t.getStatus(),
                t.getDeadline(),
                days,
                t.getCreatorId(),
                t.getAssigneeId(),
                creatorName,
                assigneeName
        );
    }

    private static String safeName(Map<UUID, String> map, UUID id) {
        if (id == null) return "-";
        String v = map.get(id);
        if (v == null || v.isBlank()) return id.toString();
        return v;
    }
}
