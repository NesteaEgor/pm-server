package ru.nesterov.pmserver.features.users.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateMyProfileRequest {

    @Size(min = 2, max = 64)
    private String displayName;

    @Size(max = 160)
    private String status;
}
