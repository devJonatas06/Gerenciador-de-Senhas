package com.project.passwordmanager.vault.dto;

public record VaultEntryExportDTO(
        Long id,

        String title,

        String email,

        String url,

        String notes,

        String passwordEncrypted) {
}
