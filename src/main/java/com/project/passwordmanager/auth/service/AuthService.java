package com.project.passwordmanager.auth.service;

import com.project.passwordmanager.auth.dto.LoginRequestDto;
import com.project.passwordmanager.auth.dto.RegisterRequestDto;
import com.project.passwordmanager.auth.dto.ResponseDto;
import com.project.passwordmanager.auth.entity.User;
import com.project.passwordmanager.auth.infra.security.PasswordStrengthValidator;
import com.project.passwordmanager.auth.repository.UserRepository;
import com.project.passwordmanager.vault.service.AuditService;
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

    public ResponseEntity<?> login(LoginRequestDto body) {
        try {
            log.info("Auth | Login attempt | email={}", body.email());

            if (loginAttemptService.isBlocked(body.email())) {
                log.warn(
                        "Auth | Login blocked | email={} | reason=MAX_ATTEMPTS",
                        body.email()
                );
                return ResponseEntity.status(429)
                        .body("Too many login attempts. Try again later.");
            }

            User user = repository.findByEmail(body.email())
                    .orElseThrow(() -> {
                        log.warn(
                                "Auth | Login failed | email={} | reason=USER_NOT_FOUND",
                                body.email()
                        );
                        return new RuntimeException("User not found");
                    });

            if (passwordEncoder.matches(body.password(), user.getPassword())) {
                loginAttemptService.loginSucceeded(body.email());
                auditService.recordAction(user.getEmail(), "LOGIN_SUCCESS");

                log.info(
                        "Auth | Login success | userId={} | email={}",
                        user.getId(),
                        user.getEmail()
                );

                String token = tokenService.genareteToken(user);
                Map<String, String> response = new HashMap<>();
                response.put("token", token);

                return ResponseEntity.ok(response);
            } else {
                log.warn(
                        "Auth | Login failed | email={} | reason=INVALID_PASSWORD",
                        body.email()
                );
                loginAttemptService.loginFailed(body.email());
                auditService.recordAction(body.email(), "LOGIN_FAILED");
                return ResponseEntity.status(401).body("Invalid credentials");
            }

        } catch (Exception exception) {
            log.error(
                    "Auth | Login error | email={}",
                    body.email(),
                    exception
            );
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    public ResponseEntity<?> register(RegisterRequestDto body) {
        try {
            passwordStrengthValidator.validate(body.password());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        Optional<User> user = repository.findByEmail(body.email());
        if (user.isPresent()) {
            return ResponseEntity.badRequest()
                    .body("This email already exists, try another");
        }

        User newUser = new User();
        newUser.setEmail(body.email());
        newUser.setName(body.name());
        newUser.setPassword(passwordEncoder.encode(body.password()));

        repository.save(newUser);
        auditService.recordAction(newUser.getEmail(), "REGISTER_NEW_USER");

        log.info(
                "Auth | Registration success | userId={} | email={}",
                newUser.getId(),
                newUser.getEmail()
        );

        String token = tokenService.genareteToken(newUser);
        return ResponseEntity.ok(new ResponseDto(newUser.getName(), token));
    }
}