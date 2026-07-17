package com.workspace.service;

import com.workspace.dto.user.request.AuthRequest;
import com.workspace.dto.user.response.AuthResponse;
import com.workspace.entity.AppUser;
import com.workspace.exception.InvalidCredentialsException;
import com.workspace.repository.UserRepository;
import com.workspace.security.jwt.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse auth(AuthRequest request) {
        AppUser user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Correo electrónico o contraseña inválidos"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Correo electrónico o contraseña inválidos");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token);
    }
}
