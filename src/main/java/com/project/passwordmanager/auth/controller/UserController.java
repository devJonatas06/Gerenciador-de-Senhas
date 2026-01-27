package com.project.passwordmanager.auth.controller;

import com.project.passwordmanager.auth.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping
    public ResponseEntity<String> getUser(@AuthenticationPrincipal User user) {
        String mensagem = "Olá " + user.getName() + "! Seu email é: " + user.getEmail();
        return ResponseEntity.ok(mensagem);
    }
}