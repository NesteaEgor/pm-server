package ru.nesterov.pmserver.features.tasks.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.nesterov.pmserver.features.projects.service.ProjectService;
import ru.nesterov.pmserver.features.tasks.dto.TaskReportRowDto;
import ru.nesterov.pmserver.features.tasks.service.TaskReportPdfBuilder;
import ru.nesterov.pmserver.features.tasks.service.TaskReportService;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/tasks/report")
public class TaskReportController {

    private final TaskReportService reportService;
    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<byte[]> generate(
            Authentication auth,
            @PathVariable UUID projectId,
            @RequestParam(required = false) String role,   // creator | assignee
            @RequestParam(required = false) String status  // TODO / IN_PROGRESS / DONE
    ) {
        UUID userId = (UUID) auth.getPrincipal();

        List<TaskReportRowDto> rows = reportService.buildReport(userId, projectId, role, status);
        var project = projectService.get(userId, projectId);

        int total = rows.size();
        int todo = 0;
        int inProgress = 0;
        int done = 0;

        for (TaskReportRowDto r : rows) {
            String s = (r.getStatus() == null ? "" : r.getStatus().trim().toUpperCase(Locale.ROOT));
            switch (s) {
                case "TODO" -> todo++;
                case "IN_PROGRESS" -> inProgress++;
                case "DONE" -> done++;
            }
        }

        byte[] pdf = TaskReportPdfBuilder.build(project.getName(), rows, total, todo, inProgress, done);

        String safeName = project.getName()
                .replaceAll("[^a-zA-Z0-9а-яА-Я _\\-\\.]", "")
                .trim()
                .replace(' ', '_');
        if (safeName.isBlank()) safeName = "project";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=task-report-" + safeName + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
