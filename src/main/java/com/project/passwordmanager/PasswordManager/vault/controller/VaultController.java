package com.project.passwordmanager.PasswordManager.vault.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.passwordmanager.PasswordManager.auth.dto.UserPrincipal;
import com.project.passwordmanager.PasswordManager.vault.dto.*;
import com.project.passwordmanager.PasswordManager.vault.service.VaultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/vault")
@RequiredArgsConstructor
@Tag(name = "Cofre de Senhas", description = "Endpoints para gerenciamento de cofres e entradas de senhas")
public class VaultController {
    private final VaultService vaultService;

    @Operation(summary = "Criar cofre", description = "Cria um novo cofre para o usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cofre criado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PostMapping
    public ResponseEntity<VaultResponseDTO> createVault(
            @Valid @RequestBody VaultRequestDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        VaultResponseDTO vault = vaultService.createVault(request, userPrincipal);
        return ResponseEntity.ok(vault);
    }

    @GetMapping
    public ResponseEntity<List<VaultResponseDTO>> getUserVaults(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<VaultResponseDTO> vaults = vaultService.getUserVaults(userPrincipal);
        return ResponseEntity.ok(vaults);
    }

    @Operation(summary = "Adicionar entrada", description = "Adiciona uma nova entrada de senha ao cofre")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entrada adicionada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado ao cofre!")
    })
    @PostMapping("/{vaultId}/entries")
    public ResponseEntity<VaultEntryResponseDTO> addEntry(
            @PathVariable Long vaultId,
            @Valid @RequestBody VaultEntryRequestDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        VaultEntryResponseDTO response = vaultService.addEntry(vaultId, request, userPrincipal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{vaultId}/entries")
    public ResponseEntity<List<VaultEntryResponseDTO>> getVaultEntries(
            @PathVariable Long vaultId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<VaultEntryResponseDTO> entries = vaultService.getVaultEntries(vaultId, userPrincipal);
        return ResponseEntity.ok(entries);
    }

    @PutMapping("/{vaultId}")
    public ResponseEntity<VaultResponseDTO> updateVault(
            @PathVariable Long vaultId,
            @Valid @RequestBody VaultRequestDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        VaultResponseDTO vault = vaultService.updateVault(vaultId, request, userPrincipal);
        return ResponseEntity.ok(vault);
    }

    @DeleteMapping("/{vaultId}")
    public ResponseEntity<String> deleteVault(
            @PathVariable Long vaultId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        vaultService.deleteVault(vaultId, userPrincipal);
        return ResponseEntity.ok("Vault deleted successfully");
    }

    @DeleteMapping("/{vaultId}/entries/{entryId}")
    public ResponseEntity<String> deleteEntry(
            @PathVariable Long vaultId,
            @PathVariable Long entryId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        vaultService.deleteEntry(vaultId, entryId, userPrincipal);
        return ResponseEntity.ok("Entry deleted successfully");
    }

    @Operation(summary = "Exportar cofres", description = "Exporta todos os cofres do usuário em formato JSON")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exportação realizada com sucesso")
    })
    @GetMapping("/export")
    public ResponseEntity<String> exportVaults(@AuthenticationPrincipal UserPrincipal userPrincipal) throws JsonProcessingException {
        List<VaultResponseDTO> vaults = vaultService.getUserVaults(userPrincipal);

        // Converter para DTOs de exportação
        List<VaultExportDto> vaultExportDTOs = vaults.stream().map(vaultDTO -> {
            List<VaultEntryExportDTO> entryDTOs = vaultDTO.entries().stream()
                    .map(entry -> new VaultEntryExportDTO(
                            entry.id(),
                            entry.title(),
                            entry.email(),
                            entry.url(),
                            entry.notes(),
                            entry.passwordEncrypted()
                    ))
                    .collect(Collectors.toList());

            return new VaultExportDto(
                    vaultDTO.id(),
                    vaultDTO.vaultName(),
                    vaultDTO.vaultKey(),
                    entryDTOs
            );
        }).collect(Collectors.toList());

        String json = new ObjectMapper().writeValueAsString(vaultExportDTOs);
        return ResponseEntity.ok(json);
    }
}