package ru.nesterov.pmserver.features.projects.members.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddProjectMemberRequest {

    @NotBlank
    @Email
    private String email;
}
