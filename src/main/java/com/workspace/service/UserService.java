package com.workspace.service;

import com.workspace.config.EmailProperties;
import com.workspace.dto.user.request.AuthRequest;
import com.workspace.dto.user.request.UserRequest;
import com.workspace.dto.user.response.AuthResponse;
import com.workspace.dto.user.response.UserResponse;
import com.workspace.entity.AppUser;
import com.workspace.entity.Role;
import com.workspace.exception.BusinessRuleException;
import com.workspace.exception.InvalidCredentialsException;
import com.workspace.exception.ResourceNotFoundException;
import com.workspace.mapper.UserMapper;
import com.workspace.repository.UserRepository;
import com.workspace.security.jwt.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final EmailProperties emailProperties;

    public UserService(UserRepository userRepository, UserMapper userMapper,
                        PasswordEncoder passwordEncoder, JwtService jwtService,
                        EmailService emailService, EmailProperties emailProperties) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.emailProperties = emailProperties;
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

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse findById(Long id) {
        return userMapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new BusinessRuleException("La contraseña es obligatoria para crear un usuario");
        }

        AppUser user = new AppUser();
        userMapper.requestToEntity(user, request);
        user.setRole(resolveRoleForCreate(request.role()));
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setCreatedAt(OffsetDateTime.now());
        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("Ya existe un usuario registrado con el correo " + request.email());
        }

        EmailProperties.Message welcome = emailProperties.welcome();
        emailService.sendEmail(user.getEmail(), welcome.subject(), welcome.body());

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        AppUser user = getOrThrow(id);
        userMapper.requestToEntity(user, request);
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("Ya existe un usuario registrado con el correo " + request.email());
        }
        return userMapper.toResponse(user);
    }

    @Transactional
    public void delete(Long id) {
        AppUser user = getOrThrow(id);
        userRepository.delete(user);
        userRepository.flush();
    }

    private Role resolveRoleForCreate(Role role) {
        if (role == null) {
            return Role.USER;
        }
        if (role == Role.ADMIN && !callerIsAdmin()) {
            throw new AccessDeniedException("Solo un administrador puede crear otro usuario administrador");
        }
        return role;
    }

    private boolean callerIsAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private AppUser getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con id " + id + " no encontrado"));
    }
}
