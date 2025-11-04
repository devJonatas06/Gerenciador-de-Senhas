package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.dto;

import jakarta.validation.constraints.NotBlank;

public record VaultAccessDTO(
        @NotBlank String vaultPassword // Para vaults protegidos
) {
}
