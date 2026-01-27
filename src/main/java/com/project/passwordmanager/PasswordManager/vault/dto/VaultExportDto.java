package com.project.passwordmanager.PasswordManager.vault.dto;

import java.util.List;

public record VaultExportDto(
        Long id,

        String vaultName,

        String vaultKey,

        List<VaultEntryExportDTO> entries
) {
}
