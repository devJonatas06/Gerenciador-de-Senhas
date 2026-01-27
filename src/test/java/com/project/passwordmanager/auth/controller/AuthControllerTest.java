package com.project.passwordmanager.auth.controller;

import com.project.passwordmanager.auth.domain.User;
import com.project.passwordmanager.auth.dto.LoginRequestDto;
import com.project.passwordmanager.auth.dto.RegisterRequestDto;
import com.project.passwordmanager.auth.infra.security.LoginAttemptService;
import com.project.passwordmanager.auth.infra.security.PasswordStrengthValidator;
import com.project.passwordmanager.auth.infra.security.TokenService;
import com.project.passwordmanager.auth.repository.UserRepository;
import com.project.passwordmanager.vault.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private PasswordStrengthValidator passwordStrengthValidator;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthController authController;

    @Test
    void whenLoginWithValidCredentials_thenReturnToken() {

        LoginRequestDto loginRequest = new LoginRequestDto("test@test.com", "password123");
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("encodedPassword");

        when(loginAttemptService.isBlocked("test@test.com")).thenReturn(false);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(tokenService.genareteToken(user)).thenReturn("jwt-token");


        ResponseEntity<?> response = authController.login(loginRequest);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(loginAttemptService).loginSucceeded("test@test.com");
        verify(auditService).recordAction("test@test.com", "LOGIN_SUCCESS");
    }

    @Test
    void whenLoginWithInvalidCredentials_thenReturnUnauthorized() {

        LoginRequestDto loginRequest = new LoginRequestDto("test@test.com", "wrongpassword");
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("encodedPassword");

        when(loginAttemptService.isBlocked("test@test.com")).thenReturn(false);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        ResponseEntity<?> response = authController.login(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody());
        verify(loginAttemptService).loginFailed("test@test.com");
        verify(auditService).recordAction("test@test.com", "LOGIN_FAILED");
    }

    @Test
    void whenLoginWithBlockedEmail_thenReturnTooManyRequests() {
        LoginRequestDto loginRequest = new LoginRequestDto("blocked@test.com", "password123");

        when(loginAttemptService.isBlocked("blocked@test.com")).thenReturn(true);

        ResponseEntity<?> response = authController.login(loginRequest);


        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("Too many login attempts. Try again later.", response.getBody());
    }

    @Test
    void whenRegisterWithValidData_thenReturnSuccess() {

        RegisterRequestDto registerRequest = new RegisterRequestDto("John Doe", "john@test.com", "StrongPass123!");

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("StrongPass123!")).thenReturn("encodedPassword");
        when(tokenService.genareteToken(any(User.class))).thenReturn("jwt-token");


        ResponseEntity<?> response = authController.register(registerRequest);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).save(any(User.class));
        verify(auditService).recordAction("john@test.com", "REGISTER_NEW_USER");
    }

    @Test
    void whenRegisterWithExistingEmail_thenReturnBadRequest() {

        RegisterRequestDto registerRequest = new RegisterRequestDto("John Doe", "existing@test.com", "StrongPass123!");
        User existingUser = new User();

        when(userRepository.findByEmail("existing@test.com")).thenReturn(Optional.of(existingUser));


        ResponseEntity<?> response = authController.register(registerRequest);


        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("This email already exists, try another", response.getBody());
    }

    @Test
    void whenRegisterWithWeakPassword_thenReturnBadRequest() {

        RegisterRequestDto registerRequest = new RegisterRequestDto("John Doe", "john@test.com", "weak");

        doThrow(new IllegalArgumentException("Password too weak"))
                .when(passwordStrengthValidator).validate("weak");

        ResponseEntity<?> response = authController.register(registerRequest);


        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Password too weak", response.getBody());
    }
}