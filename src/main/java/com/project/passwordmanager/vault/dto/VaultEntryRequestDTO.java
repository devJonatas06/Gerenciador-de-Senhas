package com.project.passwordmanager.vault.dto;

public record VaultEntryRequestDTO(
        String title,
        String email,
        String password,
        String url,
        String notes
) {
}
