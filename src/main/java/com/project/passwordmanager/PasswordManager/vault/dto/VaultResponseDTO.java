package com.project.passwordmanager.PasswordManager.vault.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VaultResponseDTO(
        Long id,
        String vaultName,
        String vaultKey,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<VaultEntryResponseDTO> entries
) {}