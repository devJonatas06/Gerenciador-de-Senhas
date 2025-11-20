package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.dto;

public record VaultEntryResponseDTO(
    Long id,
    String title,
    String email,
    String url,
    String notes,
    String passwordEncrypted
) {}