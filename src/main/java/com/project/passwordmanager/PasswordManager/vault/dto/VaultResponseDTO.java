package com.project.passwordmanager.PasswordManager.vault.dto;

import java.time.LocalDateTime;
import java.util.List;

public record VaultResponseDTO(
        Long id,
        String vaultName,
        String vaultKey,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<VaultEntryResponseDTO> entries
) {}