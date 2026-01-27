package com.project.passwordmanager.PasswordManager.vault.dto;

import jakarta.validation.constraints.NotBlank;

public record VaultAccessDTO(
        @NotBlank String vaultPassword
) {
}
