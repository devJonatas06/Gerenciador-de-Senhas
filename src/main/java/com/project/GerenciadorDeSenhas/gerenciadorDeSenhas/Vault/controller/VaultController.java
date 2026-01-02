package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.LoginGerenciadorDeSenha.domain.User;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Domain.Vault;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Domain.VaultEntry;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.dto.*;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.repository.VaultEntryRepository;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.repository.VaultRepository;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.service.VaultEncryptionService;
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
    private final VaultRepository vaultRepository;
    private final VaultEncryptionService encryptionService;
    private final VaultEntryRepository vaultEntryRepository;

    @Operation(summary = "Criar cofre", description = "Cria um novo cofre para o usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cofre criado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })

    @PostMapping
    public ResponseEntity<Vault> createVault(
            @Valid @RequestBody VaultRequestDTO request,
            @AuthenticationPrincipal User user) {
        Vault vault = new Vault();
        vault.setUser(user);
        vault.setVaultName(request.vaultName());
        String hashedKey = encryptionService.hashVaultKey(request.vaultKey());
        vault.setVaultKey(hashedKey);
        Vault savedVault = vaultRepository.save(vault);
        return ResponseEntity.ok(savedVault);
    }


    @GetMapping
    public ResponseEntity<List<Vault>> getUserVaults(@AuthenticationPrincipal User user) {
        List<Vault> vaults = vaultRepository.findByUser(user);
        return ResponseEntity.ok(vaults);
    }

    @Operation(summary = "Adicionar entrada", description = "Adiciona uma nova entrada de senha ao cofre  ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entrada adicionada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado ao cofre !")
    })
    @PostMapping("/{vaultId}/entries")
    public ResponseEntity<VaultEntryResponseDTO> addEntry(
            @PathVariable Long vaultId,
            @Valid @RequestBody VaultEntryRequestDTO request,
            @AuthenticationPrincipal User user) {

        Vault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new RuntimeException("Vault not found"));

        if (!vault.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        VaultEntry entry = new VaultEntry();
        entry.setVault(vault);
        entry.setTitle(request.title());
        entry.setEmail(request.email());
        entry.setUrl(request.url());
        entry.setNotes(request.notes());
        String encryptedPassword = encryptionService.encryptPassword(request.password().toCharArray(), vault);
        entry.setPasswordEncrypted(encryptedPassword);

        VaultEntry savedEntry = vaultEntryRepository.save(entry);

        VaultEntryResponseDTO responseDTO = new VaultEntryResponseDTO(
                savedEntry.getId(),
                savedEntry.getTitle(),
                savedEntry.getEmail(),
                savedEntry.getUrl(),
                savedEntry.getNotes(),
                savedEntry.getPasswordEncrypted()
        );

        return ResponseEntity.ok(responseDTO);
    }


    @GetMapping("/{vaultId}/entries")
    public ResponseEntity<List<VaultEntryResponseDTO>> getVaultEntries(
            @PathVariable Long vaultId,
            @AuthenticationPrincipal User user) {

        Vault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new RuntimeException("Vault not found"));

        if (!vault.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        List<VaultEntry> entries = vaultEntryRepository.findByVault(vault);


        List<VaultEntryResponseDTO> responseDTOs = entries.stream()
                .map(entry -> new VaultEntryResponseDTO(
                        entry.getId(),
                        entry.getTitle(),
                        entry.getEmail(),
                        entry.getUrl(),
                        entry.getNotes(),
                        entry.getPasswordEncrypted()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    @PutMapping("/{vaultId}")
    public ResponseEntity<Vault> updateVault(@PathVariable Long vaultId, @Valid @RequestBody VaultRequestDTO request, @AuthenticationPrincipal User user) {
        Vault vault = vaultRepository.findById(vaultId).orElseThrow(() -> new RuntimeException("Vault not found"));
        if (!vault.getUser().getId().equals(user.getId())) return ResponseEntity.status(403).build();
        vault.setVaultName(request.vaultName());
        vaultRepository.save(vault);
        return ResponseEntity.ok(vault);
    }

    @DeleteMapping("/{vaultId}")
    public ResponseEntity<String> deleteVault(@PathVariable Long vaultId, @AuthenticationPrincipal User user) {
        Vault vault = vaultRepository.findById(vaultId).orElseThrow(() -> new RuntimeException("Vault not found"));
        if (!vault.getUser().getId().equals(user.getId())) return ResponseEntity.status(403).build();
        vaultRepository.delete(vault);
        return ResponseEntity.ok("Vault deleted successfully");
    }

    @DeleteMapping("/{vaultId}/entries/{entryId}")
    public ResponseEntity<String> deleteEntry(@PathVariable Long vaultId, @PathVariable Long entryId, @AuthenticationPrincipal User user) {
        Vault vault = vaultRepository.findById(vaultId).orElseThrow(() -> new RuntimeException("Vault not found"));
        if (!vault.getUser().getId().equals(user.getId())) return ResponseEntity.status(403).build();
        vaultEntryRepository.deleteById(entryId);
        return ResponseEntity.ok("Entry deleted successfully");
    }

    @Operation(summary = "Exportar cofres", description = "Exporta todos os cofres do usuário em formato JSON")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exportação realizada com sucesso")
    })
    @GetMapping("/export")
    public ResponseEntity<String> exportVaults(@AuthenticationPrincipal User user) throws JsonProcessingException {
        List<Vault> vaults = vaultRepository.findByUser(user);
        List<VaultExportDto> vaultExportDTOs = vaults.stream().map(vault -> {
            List<VaultEntryExportDTO> entryDTOs = vault.getEntries().stream().map(entry ->
                    new VaultEntryExportDTO(
                            entry.getId(),
                            entry.getTitle(),
                            entry.getEmail(),
                            entry.getUrl(),
                            entry.getNotes(),
                            entry.getPasswordEncrypted()
                    )
            ).collect(Collectors.toList());

            return new VaultExportDto(
                    vault.getId(),
                    vault.getVaultName(),
                    vault.getVaultKey(),
                    entryDTOs
            );
        }).collect(Collectors.toList());

        String json = new ObjectMapper().writeValueAsString(vaultExportDTOs);
        return ResponseEntity.ok(json);
    }

    @Operation(summary = "Importar cofres", description = "Importa cofres a partir de um JSON")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Importação realizada com sucesso")
    })
    @PostMapping("/import")
    public ResponseEntity<String> importVaults(@AuthenticationPrincipal User user, @RequestBody String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<VaultExportDto> importedVaultDTOs = mapper.readValue(json, new TypeReference<List<VaultExportDto>>() {
        });

        importedVaultDTOs.forEach(vaultDTO -> {
            Vault vault = new Vault();
            vault.setUser(user);
            vault.setVaultName(vaultDTO.vaultName());
            vault.setVaultKey(vaultDTO.vaultKey());

            Vault savedVault = vaultRepository.save(vault);

            vaultDTO.entries().forEach(entryDTO -> {
                VaultEntry entry = new VaultEntry();
                entry.setVault(savedVault);
                entry.setTitle(entryDTO.title());
                entry.setEmail(entryDTO.email());
                entry.setUrl(entryDTO.url());
                entry.setNotes(entryDTO.notes());
                entry.setPasswordEncrypted(entryDTO.passwordEncrypted());
                vaultEntryRepository.save(entry);
            });
        });

        return ResponseEntity.ok("Vaults imported successfully");
    }
}
