package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.controller;

import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.LoginGerenciadorDeSenha.domain.User;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Domain.Vault;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Domain.VaultEntry;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.dto.VaultEntryRequestDTO;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.dto.VaultRequestDTO;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.repository.VaultEntryRepository;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.repository.VaultLoginRepository;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.repository.VaultRepository;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.service.VaultEncryptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vault")
@RequiredArgsConstructor
public class VaultController {

    private final VaultRepository vaultRepository;
    private final VaultEncryptionService encryptionService;
    private final VaultEntryRepository vaultEntryRepository;


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

    @PostMapping("/{vaultId}/entries")
    public ResponseEntity<VaultEntry> addEntry(
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
    public ResponseEntity<List<VaultEntry>> getVaultEntries(
            @PathVariable Long vaultId,
            @AuthenticationPrincipal User user) {

        Vault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new RuntimeException("Vault not found"));

        if (!vault.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        List<VaultEntry> entries = vaultEntryRepository.findByVault(vault);

        // Convert to DTOs
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

    @PostMapping("/import")
    public ResponseEntity<String> importVaults(@AuthenticationPrincipal User user, @RequestBody String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<VaultExportDto> importedVaultDTOs = mapper.readValue(json, new TypeReference<List<VaultExportDto>>() {});

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
