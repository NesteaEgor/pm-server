package ru.nesterov.pmserver.features.tasks.dto;

import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
public class TaskReportRowDto {

    UUID taskId;
    String title;
    String status;

    Instant deadline;
    Long daysToDeadline;

    UUID creatorId;
    UUID assigneeId;

    String creatorName;
    String assigneeName;
}
