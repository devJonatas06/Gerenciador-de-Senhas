package com.project.passwordmanager.vault.dto;

import jakarta.validation.constraints.NotBlank;

public record VaultAccessDTO(
        @NotBlank String vaultPassword
) {
}
