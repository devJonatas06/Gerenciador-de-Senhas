package com.project.passwordmanager.PasswordManager.auth.controller;

import com.project.passwordmanager.PasswordManager.auth.dto.LoginRequestDto;
import com.project.passwordmanager.PasswordManager.auth.dto.RegisterRequestDto;
import com.project.passwordmanager.PasswordManager.auth.dto.ResponseDto;
import com.project.passwordmanager.PasswordManager.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDto> login(@Valid @RequestBody LoginRequestDto body) {
        ResponseDto response = authService.login(body);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDto> register(@RequestBody RegisterRequestDto body) {
        return ResponseEntity.ok(authService.register(body));
    }

}