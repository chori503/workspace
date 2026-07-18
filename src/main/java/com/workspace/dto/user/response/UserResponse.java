package com.workspace.dto.user.response;

import com.workspace.entity.Role;

import java.time.OffsetDateTime;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        Role role,
        OffsetDateTime createdAt
) {
}
