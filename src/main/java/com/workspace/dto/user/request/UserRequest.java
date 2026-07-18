package com.workspace.dto.user.request;

import com.workspace.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @Size(min = 8, max = 255)
        String password,

        @NotBlank
        @Size(max = 255)
        String fullName,

        Role role
) {
}
