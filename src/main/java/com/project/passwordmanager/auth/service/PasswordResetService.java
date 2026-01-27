package com.project.passwordmanager.auth.service;

import com.project.passwordmanager.auth.entity.User;
import com.project.passwordmanager.auth.infra.security.PasswordStrengthValidator;
import com.project.passwordmanager.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
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

        resetAttempts.putIfAbsent(email, 0);
        if (resetAttempts.get(email) >= MAX_ATTEMPTS) {
            log.warn("Many recovery attempts for email {}", email);
            return ResponseEntity.badRequest()
                    .body("Too many reset attempts. Try again later.");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid email");
        }

        resetAttempts.put(email, resetAttempts.get(email) + 1);

        String token = UUID.randomUUID().toString();
        resetTokens.put(token,
                new ResetTokenData(email, LocalDateTime.now().plusMinutes(5)));

        System.out.println(
                "Password reset link: http://localhost:8080/auth/reset-password?token=" + token
        );

        return ResponseEntity.ok("Password reset link sent.");
    }

    public ResponseEntity<String> resetPassword(String token, String newPassword) {

        ResetTokenData data = resetTokens.get(token);
        if (data == null) {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }

        if (data.expireAt().isBefore(LocalDateTime.now())) {
            resetTokens.remove(token);
            return ResponseEntity.badRequest().body("Token expired");
        }

        try {
            passwordStrengthValidator.validate(newPassword);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        User user = userRepository.findByEmail(data.email()).orElseThrow();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetTokens.remove(token);
        resetAttempts.remove(data.email());

        return ResponseEntity.ok("Password updated successfully.");
    }

    private record ResetTokenData(String email, LocalDateTime expireAt) {}
}
