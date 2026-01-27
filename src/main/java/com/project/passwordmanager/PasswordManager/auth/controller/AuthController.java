package com.project.passwordmanager.PasswordManager.auth.controller;

import com.project.passwordmanager.PasswordManager.auth.dto.LoginRequestDto;
import com.project.passwordmanager.PasswordManager.auth.dto.RegisterRequestDto;
import com.project.passwordmanager.PasswordManager.auth.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto body) {
        return authService.login(body);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto body) {
        return authService.register(body);
    }
}
