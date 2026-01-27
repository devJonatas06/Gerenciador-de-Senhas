// VaultRequestDTO.java
package com.project.passwordmanager.PasswordManager.vault.dto;

import jakarta.validation.constraints.NotBlank;

public record VaultRequestDTO(
    @NotBlank String vaultName,
    String vaultKey
) {}


