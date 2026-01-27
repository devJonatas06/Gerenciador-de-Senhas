package com.project.passwordmanager.PasswordManager.vault.dto;

public record VaultEntryRequestDTO(
        String title,
        String email,
        String password,
        String url,
        String notes
) {
}
