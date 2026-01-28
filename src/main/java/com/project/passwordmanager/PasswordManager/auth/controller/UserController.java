package com.project.passwordmanager.PasswordManager.auth.controller;

import com.project.passwordmanager.PasswordManager.auth.dto.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping
    public ResponseEntity<String> getUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        String mensagem = "Olá " + userPrincipal.getUser().getName() + "! Seu email é: " + userPrincipal.getEmail();
        return ResponseEntity.ok(mensagem);
    }
}