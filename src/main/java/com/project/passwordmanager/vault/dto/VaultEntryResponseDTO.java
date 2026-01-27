package com.project.passwordmanager.vault.dto;

public record VaultEntryResponseDTO(
    Long id,
    String title,
    String email,
    String url,
    String notes,
    String passwordEncrypted
) {}