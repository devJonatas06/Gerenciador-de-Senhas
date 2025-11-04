package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.dto;

public record VaultEntryRequestDTO(
        String title,
        String email,
        String password, // Será encryptada
        String url,
        String notes
) {
}
