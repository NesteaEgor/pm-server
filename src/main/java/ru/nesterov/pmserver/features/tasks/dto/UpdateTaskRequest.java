package ru.nesterov.pmserver.features.tasks.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTaskRequest {

    // Можно прислать только то, что меняем (nullable поля)
    @Size(min = 2, max = 255)
    private String title;

    @Size(max = 5000)
    private String description;

    // TODO / IN_PROGRESS / DONE
    private String status;
}
