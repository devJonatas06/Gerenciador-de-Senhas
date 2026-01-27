package com.project.passwordmanager.PasswordManager.auth.controller;

import com.project.passwordmanager.PasswordManager.auth.service.PasswordResetService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Recuperação de Senha")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<String> requestReset(@RequestParam String email) {
        return passwordResetService.requestReset(email);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword
    ) {
        return passwordResetService.resetPassword(token, newPassword);
    }
}
