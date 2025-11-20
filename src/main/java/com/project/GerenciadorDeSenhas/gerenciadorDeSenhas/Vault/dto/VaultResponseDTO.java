package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.dto;

import java.util.List;

public record VaultResponseDTO(
    Long id,
    String vaultName,
    List<VaultEntryResponseDTO> entries
) {}