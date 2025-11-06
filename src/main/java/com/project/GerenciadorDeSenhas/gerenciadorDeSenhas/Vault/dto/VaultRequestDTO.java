// VaultRequestDTO.java
package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.dto;

import jakarta.validation.constraints.NotBlank;

public record VaultRequestDTO(
    @NotBlank String vaultName,
    String vaultKey
) {}


