package com.workspace.controller;

import com.workspace.dto.user.request.AuthRequest;
import com.workspace.dto.user.response.AuthResponse;
import com.workspace.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/auth")
    public AuthResponse auth(@Valid @RequestBody AuthRequest request) {
        return userService.auth(request);
    }
}
