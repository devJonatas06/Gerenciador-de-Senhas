package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.dto;

public record VaultEntryExportDTO(
        Long id,

        String title,

        String email,

        String url,

        String notes,

        String passwordEncrypted) {
}
