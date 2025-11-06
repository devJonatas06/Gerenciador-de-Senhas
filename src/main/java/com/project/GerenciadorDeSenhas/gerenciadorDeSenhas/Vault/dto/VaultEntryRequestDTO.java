package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.dto;

public record VaultEntryRequestDTO(
        String title,
        String email,
        String password,
        String url,
        String notes
) {
}
