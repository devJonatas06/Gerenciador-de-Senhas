package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.controller;

import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.domain.User;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.dto.LoginRequestDto;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.dto.RegisterRequestDto;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.dto.ResponseDto;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.infra.security.LoginAttemptService;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.infra.security.TokenService;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
    public class AuthController {

        private final UserRepository repository;
        private final PasswordEncoder passwordEncoder;
        private final TokenService tokenService;
        private final LoginAttemptService loginAttemptService;

        @PostMapping("/login")
        public ResponseEntity login(@Valid @RequestBody LoginRequestDto body) {
            log.info("Tentando login com email: {}", body.email());

            // bloqueio por brute-force
            if (loginAttemptService.isBlocked(body.email())) {
                log.warn("Usuário {} bloqueado por muitas tentativas", body.email());
                return ResponseEntity.status(429).body("Too many login attempts. Try again later.");
            }

            User user = this.repository.findByEmail(body.email())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (passwordEncoder.matches(body.password(), user.getPassword())) {
                loginAttemptService.loginSucceeded(body.email());
                log.info("Login bem-sucedido para {}", user.getEmail());
                String token = this.tokenService.genareteToken(user);
                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                return ResponseEntity.ok(response);

            } else {
                loginAttemptService.loginFailed(body.email());
                log.warn("Falha no login para {}", body.email());
                return ResponseEntity.status(401).body("Invalid credentials");
            }
        }

        @PostMapping("/register")
        public ResponseEntity register(@RequestBody RegisterRequestDto body) {
            Optional<User> user = this.repository.findByEmail(body.email());
            if (user.isEmpty()) {
                User newUser = new User();
                newUser.setPassword(passwordEncoder.encode(body.password()));
                newUser.setEmail(body.email());
                newUser.setName(body.name());
                this.repository.save(newUser);
                String token = this.tokenService.genareteToken(newUser);
                return ResponseEntity.ok(new ResponseDto(newUser.getName(), token));
            }else {
                return ResponseEntity.badRequest().build();
            }
        }
    }
