package com.workspace.mapper;

import com.workspace.dto.user.request.UserRequest;
import com.workspace.dto.user.response.UserResponse;
import com.workspace.entity.AppUser;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    public void requestToEntity(AppUser user, UserRequest request) {
        user.setEmail(request.email());
        user.setFullName(request.fullName());
    }
}
