package ru.nesterov.pmserver.features.tasks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CreateTaskRequest {

    @NotBlank
    @Size(min = 2, max = 255)
    private String title;

    @Size(max = 5000)
    private String description;

    private Instant deadline;

    private UUID assigneeId;
}
