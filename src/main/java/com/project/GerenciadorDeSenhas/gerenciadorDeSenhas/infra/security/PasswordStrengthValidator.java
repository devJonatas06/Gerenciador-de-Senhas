package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.infra.security;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class PasswordStrengthValidator {

    private static final String REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$";

    public void validate(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("The password cannot be empty.");
        }

        if (password.length() < 12) {
            throw new IllegalArgumentException("The password must be at least 12 characters long. including uppercase and lowercase letters, special characters, and symbols ");
        }

        if (!Pattern.matches(REGEX, password)) {
            throw new IllegalArgumentException("The password must contain uppercase letters, lowercase letters, numbers and special characters.");
        }
    }
}
