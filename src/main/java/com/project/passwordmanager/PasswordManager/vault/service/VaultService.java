package com.project.passwordmanager.PasswordManager.vault.service;

import com.project.passwordmanager.PasswordManager.auth.dto.UserPrincipal;
import com.project.passwordmanager.PasswordManager.auth.entity.User;
import com.project.passwordmanager.PasswordManager.auth.exception.ResourceNotFoundException;
import com.project.passwordmanager.PasswordManager.vault.dto.*;
import com.project.passwordmanager.PasswordManager.vault.entity.Vault;
import com.project.passwordmanager.PasswordManager.vault.entity.VaultEntry;
import com.project.passwordmanager.PasswordManager.vault.exception.VaultAccessDeniedException;
import com.project.passwordmanager.PasswordManager.vault.repository.VaultEntryRepository;
import com.project.passwordmanager.PasswordManager.vault.repository.VaultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultService {
    private final VaultRepository vaultRepository;
    private final VaultEntryRepository vaultEntryRepository;
    private final VaultEncryptionService encryptionService;

    public VaultResponseDTO createVault(VaultRequestDTO request, UserPrincipal userPrincipal) {
        log.info("Creating vault for user: {}", userPrincipal.getId());

        Vault vault = new Vault();
        vault.setUser(userPrincipal.getUser());
        vault.setVaultName(request.vaultName());

        if (request.vaultKey() != null && !request.vaultKey().isEmpty()) {
            String hashedKey = encryptionService.hashVaultKey(request.vaultKey());
            vault.setVaultKey(hashedKey);
        }

        Vault savedVault = vaultRepository.save(vault);
        return toVaultResponseDTO(savedVault);
    }

    public List<VaultResponseDTO> getUserVaults(UserPrincipal userPrincipal) {
        List<Vault> vaults = vaultRepository.findByUser(userPrincipal.getUser());

        return vaults.stream()
                .map(this::toVaultResponseDTO)
                .collect(Collectors.toList());
    }

    public Vault getVaultEntityById(Long vaultId, UserPrincipal userPrincipal) {
        Vault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new ResourceNotFoundException("Vault not found"));

        validateVaultOwnership(vault, userPrincipal);
        return vault;
    }

    public VaultEntryResponseDTO addEntry(Long vaultId, VaultEntryRequestDTO request, UserPrincipal userPrincipal) {
        Vault vault = getVaultEntityById(vaultId, userPrincipal);

        VaultEntry entry = new VaultEntry();
        entry.setVault(vault);
        entry.setTitle(request.title());
        entry.setEmail(request.email());
        entry.setUrl(request.url());
        entry.setNotes(request.notes());

        if (request.password() != null) {
            String encryptedPassword = encryptionService.encryptPassword(request.password().toCharArray(), vault);
            entry.setPasswordEncrypted(encryptedPassword);
        }

        VaultEntry savedEntry = vaultEntryRepository.save(entry);

        return toVaultEntryResponseDTO(savedEntry);
    }

    public List<VaultEntryResponseDTO> getVaultEntries(Long vaultId, UserPrincipal userPrincipal) {
        Vault vault = getVaultEntityById(vaultId, userPrincipal);
        List<VaultEntry> entries = vaultEntryRepository.findByVault(vault);

        return entries.stream()
                .map(this::toVaultEntryResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public VaultResponseDTO updateVault(Long vaultId, VaultRequestDTO request, UserPrincipal userPrincipal) {
        Vault vault = getVaultEntityById(vaultId, userPrincipal);

        if (request.vaultName() != null && !request.vaultName().isEmpty()) {
            vault.setVaultName(request.vaultName());
        }

        Vault updatedVault = vaultRepository.save(vault);
        return toVaultResponseDTO(updatedVault);
    }

    @Transactional
    public void deleteVault(Long vaultId, UserPrincipal userPrincipal) {
        Vault vault = getVaultEntityById(vaultId, userPrincipal);
        vaultRepository.delete(vault);
        log.info("Vault deleted: {}", vaultId);
    }

    @Transactional
    public void deleteEntry(Long vaultId, Long entryId, UserPrincipal userPrincipal) {
        getVaultEntityById(vaultId, userPrincipal); // Validate ownership
        vaultEntryRepository.deleteById(entryId);
        log.info("Entry deleted: {}", entryId);
    }

    private void validateVaultOwnership(Vault vault, UserPrincipal userPrincipal) {
        if (!vault.getUser().getId().equals(userPrincipal.getId())) {
            throw new VaultAccessDeniedException("Access denied to vault");
        }
    }

    private VaultResponseDTO toVaultResponseDTO(Vault vault) {
        List<VaultEntryResponseDTO> entryDTOs = vault.getEntries().stream()
                .map(this::toVaultEntryResponseDTO)
                .collect(Collectors.toList());

        return new VaultResponseDTO(
                vault.getId(),
                vault.getVaultName(),
                vault.getVaultKey(),
                vault.getCreatedAt(),
                vault.getUpdatedAt(),
                entryDTOs
        );
    }

    private VaultEntryResponseDTO toVaultEntryResponseDTO(VaultEntry entry) {
        return new VaultEntryResponseDTO(
                entry.getId(),
                entry.getTitle(),
                entry.getEmail(),
                entry.getUrl(),
                entry.getNotes(),
                entry.getPasswordEncrypted()
        );
    }
}