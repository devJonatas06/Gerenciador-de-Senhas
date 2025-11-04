// VaultRequestDTO.java
package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.dto;

import jakarta.validation.constraints.NotBlank;

public record VaultRequestDTO(
    @NotBlank String vaultName,
    String vaultKey // Opcional para vaults com senha adicional
) {}

// VaultEntryRequestDTO.java
package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.dto;

public record VaultEntryRequestDTO(
    String title,
    String email,
    String password, // Será encryptada
    String url,
    String notes
) {}

// VaultAccessDTO.java
package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.dto;

public record VaultAccessDTO(
    @NotBlank String vaultPassword // Para vaults protegidos
) {}