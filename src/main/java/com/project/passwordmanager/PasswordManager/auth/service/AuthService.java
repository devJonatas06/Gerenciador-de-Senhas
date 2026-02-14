package com.project.passwordmanager.PasswordManager.auth.service;

import com.project.passwordmanager.PasswordManager.auth.dto.LoginRequestDto;
import com.project.passwordmanager.PasswordManager.auth.dto.RegisterRequestDto;
import com.project.passwordmanager.PasswordManager.auth.dto.ResponseDto;
import com.project.passwordmanager.PasswordManager.auth.entity.User;
import com.project.passwordmanager.PasswordManager.auth.exception.BusinessException;
import com.project.passwordmanager.PasswordManager.auth.exception.ResourceNotFoundException;
import com.project.passwordmanager.PasswordManager.auth.infra.security.PasswordStrengthValidator;
import com.project.passwordmanager.PasswordManager.auth.repository.UserRepository;
import com.project.passwordmanager.PasswordManager.vault.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final LoginAttemptService loginAttemptService;
    private final PasswordStrengthValidator passwordStrengthValidator;
    private final AuditService auditService;

    public ResponseDto login(LoginRequestDto body) {

        log.info("Auth | Login attempt | email={}", body.email());

        if (loginAttemptService.isBlocked(body.email())) {
            throw new SecurityException("Too many login attempts. Try again later.");
        }

        User user = repository.findByEmail(body.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(body.password(), user.getPassword())) {
            loginAttemptService.loginFailed(body.email());
            auditService.recordAction(body.email(), "LOGIN_FAILED");
            throw new SecurityException("Invalid credentials");
        }

        loginAttemptService.loginSucceeded(body.email());
        auditService.recordAction(user.getEmail(), "LOGIN_SUCCESS");

        String token = tokenService.generateToken(user);

        return new ResponseDto(user.getName(), token);
    }

    public ResponseDto register(RegisterRequestDto body) {

        passwordStrengthValidator.validate(body.password());

        if (repository.findByEmail(body.email()).isPresent()) {
            throw new BusinessException("This email already exists, try another");
        }

        User newUser = new User();
        newUser.setEmail(body.email());
        newUser.setName(body.name());
        newUser.setPassword(passwordEncoder.encode(body.password()));

        repository.save(newUser);
        auditService.recordAction(newUser.getEmail(), "REGISTER_NEW_USER");

        String token = tokenService.generateToken(newUser);

        return new ResponseDto(newUser.getName(), token);
    }
}
