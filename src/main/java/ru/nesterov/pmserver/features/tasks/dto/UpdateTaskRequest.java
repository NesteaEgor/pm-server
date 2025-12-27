package ru.nesterov.pmserver.features.tasks.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class UpdateTaskRequest {

    @Size(min = 2, max = 255)
    private String title;

    @Size(max = 5000)
    private String description;

    // TODO / IN_PROGRESS / DONE
    private String status;

    private Instant deadline;

    private UUID assigneeId;
}
