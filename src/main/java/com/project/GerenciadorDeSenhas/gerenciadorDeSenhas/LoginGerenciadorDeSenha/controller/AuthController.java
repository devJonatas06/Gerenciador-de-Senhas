package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.LoginGerenciadorDeSenha.controller;

import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.LoginGerenciadorDeSenha.domain.User;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.LoginGerenciadorDeSenha.dto.LoginRequestDto;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.LoginGerenciadorDeSenha.dto.RegisterRequestDto;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.LoginGerenciadorDeSenha.dto.ResponseDto;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.LoginGerenciadorDeSenha.infra.security.LoginAttemptService;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.LoginGerenciadorDeSenha.infra.security.PasswordStrengthValidator;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.LoginGerenciadorDeSenha.infra.security.TokenService;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.LoginGerenciadorDeSenha.repository.UserRepository;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Autenticação", description = "Endpoints para registro, login e gestão de autenticação")
public class AuthController {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final LoginAttemptService loginAttemptService;
    private final PasswordStrengthValidator passwordStrengthValidator;
    private final AuditService auditService;


    @Operation(summary = "Login de usuário", description = "Autentica um usuário e retorna um token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "429", description = "Muitas tentativas de login")
    })
    @PostMapping("/login")
    public ResponseEntity login(@Valid @RequestBody LoginRequestDto body) {
        log.info("Tentando login com email: {}", body.email());

        if (loginAttemptService.isBlocked(body.email())) {
            log.warn("User {} blocked by too many attempts", body.email());
            return ResponseEntity.status(429).body("Too many login attempts. Try again later.");
        }

        User user = this.repository.findByEmail(body.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (passwordEncoder.matches(body.password(), user.getPassword())) {
            loginAttemptService.loginSucceeded(body.email());
            auditService.recordAction(user.getEmail(), "LOGIN_SUCCESS");
            log.info("Login bem-sucedido para {}", user.getEmail());
            String token = this.tokenService.genareteToken(user);
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);

        } else {
            loginAttemptService.loginFailed(body.email());
            auditService.recordAction(body.email(), "LOGIN_FAILED");
            log.warn("Falha no login para {}", body.email());
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @Operation(summary = "Registro de usuário", description = "Cria uma nova conta de usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Email já existe ou senha fraca")
    })
    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequestDto body) {
        try {
            passwordStrengthValidator.validate(body.password());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        Optional<User> user = this.repository.findByEmail(body.email());
        if (user.isEmpty()) {
            User newUser = new User();
            newUser.setPassword(passwordEncoder.encode(body.password()));
            newUser.setEmail(body.email());
            newUser.setName(body.name());
            this.repository.save(newUser);

            auditService.recordAction(newUser.getEmail(), "REGISTER_NEW_USER");
            String token = this.tokenService.genareteToken(newUser);
            return ResponseEntity.ok(new ResponseDto(newUser.getName(), token));
        } else {
            return ResponseEntity.badRequest().body("This email already exists, try another");
        }
    }
}