package com.project.passwordmanager.PasswordManager.auth.service;

import com.project.passwordmanager.PasswordManager.auth.entity.User;
import com.project.passwordmanager.PasswordManager.auth.infra.security.PasswordStrengthValidator;
import com.project.passwordmanager.PasswordManager.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordStrengthValidator passwordStrengthValidator;

    private static final ConcurrentHashMap<String, ResetTokenData> resetTokens = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Integer> resetAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;

    public ResponseEntity<String> requestReset(String email) {
        // Tentativa de reset
        log.info(
                "PasswordReset | Request attempt | email={}",
                email
        );

        resetAttempts.putIfAbsent(email, 0);

        // Verifica se excedeu tentativas
        if (resetAttempts.get(email) >= MAX_ATTEMPTS) {
            log.warn(
                    "PasswordReset | Request blocked | email={} | attempts={}",
                    email,
                    resetAttempts.get(email)
            );
            return ResponseEntity.badRequest()
                    .body("Too many reset attempts. Try again later.");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);

        // Verifica se usuário existe (log interno)
        if (userOpt.isEmpty()) {
            log.warn(
                    "PasswordReset | Request failed | email={} | reason=USER_NOT_FOUND",
                    email
            );
            resetAttempts.put(email, resetAttempts.get(email) + 1);
            return ResponseEntity.badRequest().body("Invalid email");
        }

        // Incrementa tentativas
        resetAttempts.put(email, resetAttempts.get(email) + 1);

        // Gera token
        String token = UUID.randomUUID().toString();
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(5);
        resetTokens.put(token, new ResetTokenData(email, expireAt));

        // Log de token gerado
        log.info(
                "PasswordReset | Token generated | email={} | expiresAt={}",
                email,
                expireAt
        );

        // Em produção, enviar email
        System.out.println(
                "Password reset link: http://localhost:8080/auth/reset-password?token=" + token
        );

        return ResponseEntity.ok("Password reset link sent.");
    }

    public ResponseEntity<String> resetPassword(String token, String newPassword) {
        ResetTokenData data = resetTokens.get(token);

        // Token inválido
        if (data == null) {
            log.warn(
                    "PasswordReset | Reset failed | token={} | reason=INVALID_TOKEN",
                    token
            );
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }

        // Token expirado
        if (data.expireAt().isBefore(LocalDateTime.now())) {
            log.warn(
                    "PasswordReset | Reset failed | token={} | reason=TOKEN_EXPIRED",
                    token
            );
            resetTokens.remove(token);
            return ResponseEntity.badRequest().body("Token expired");
        }

        // Valida força da senha
        try {
            passwordStrengthValidator.validate(newPassword);
        } catch (IllegalArgumentException e) {
            log.warn(
                    "PasswordReset | Reset failed | email={} | reason=WEAK_PASSWORD",
                    data.email()
            );
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        // Atualiza senha
        User user = userRepository.findByEmail(data.email()).orElseThrow();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Limpa tokens e tentativas
        resetTokens.remove(token);
        resetAttempts.remove(data.email());

        // Log de sucesso
        log.info(
                "PasswordReset | Password updated | email={}",
                data.email()
        );

        return ResponseEntity.ok("Password updated successfully.");
    }

    private record ResetTokenData(String email, LocalDateTime expireAt) {}
}