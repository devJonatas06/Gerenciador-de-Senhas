package com.project.passwordmanager.vault.dto;

import java.util.List;

public record VaultResponseDTO(
    Long id,
    String vaultName,
    List<VaultEntryResponseDTO> entries
) {}