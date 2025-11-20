package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.LoginGerenciadorDeSenha.controller;

import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.LoginGerenciadorDeSenha.domain.User;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Domain.Vault;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Domain.VaultEntry;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.controller.VaultController;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.dto.VaultEntryRequestDTO;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.dto.VaultRequestDTO;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.repository.VaultEntryRepository;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.repository.VaultRepository;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.service.VaultEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.http.ResponseEntity;

class VaultControllerTest {

    private VaultRepository vaultRepository;
    private VaultEntryRepository entryRepository;
    private VaultEncryptionService encryptionService;
    private VaultController controller;

    private User user;

    @BeforeEach
    void setup() {
        vaultRepository = mock(VaultRepository.class);
        entryRepository = mock(VaultEntryRepository.class);
        encryptionService = mock(VaultEncryptionService.class);

        controller = new VaultController(vaultRepository, encryptionService, entryRepository);

        user = new User();
        user.setId(1L);
        user.setEmail("user@mail.com");
    }

    @Test
    void shouldCreateVault() {
        VaultRequestDTO req = new VaultRequestDTO("MyVault", "key123");

        Vault saved = new Vault();
        saved.setId(10L);
        saved.setVaultName("MyVault");
        saved.setUser(user);

        when(encryptionService.hashVaultKey("key123")).thenReturn("hashed");
        when(vaultRepository.save(any(Vault.class))).thenReturn(saved);

        ResponseEntity<Vault> response = controller.createVault(req, user);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("MyVault", response.getBody().getVaultName());
    }

    @Test
    void shouldReturnUserVaults() {
        Vault v = new Vault();
        v.setId(1L);
        v.setVaultName("TestVault");
        v.setUser(user);

        when(vaultRepository.findByUser(user)).thenReturn(List.of(v));

        ResponseEntity<List<Vault>> response = controller.getUserVaults(user);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void shouldAddEntryToCorrectVault() {
        Vault vault = new Vault();
        vault.setId(1L);
        vault.setUser(user);

        VaultEntryRequestDTO req = new VaultEntryRequestDTO("Title", "email", "url", "notes", "pass");

        when(vaultRepository.findById(1L)).thenReturn(Optional.of(vault));
        when(encryptionService.encryptPassword(any(), eq(vault))).thenReturn("encrypted");
        when(entryRepository.save(any(VaultEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.addEntry(1L, req, user);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void shouldDenyAccessWhenVaultNotOwned() {
        Vault vault = new Vault();
        vault.setId(1L);

        User other = new User();
        other.setId(99L);

        vault.setUser(other);

        when(vaultRepository.findById(1L)).thenReturn(Optional.of(vault));

        VaultEntryRequestDTO req = new VaultEntryRequestDTO("X", "Y", "Z", "N", "P");

        ResponseEntity<?> response = controller.addEntry(1L, req, user);

        assertEquals(403, response.getStatusCodeValue());
    }
}
