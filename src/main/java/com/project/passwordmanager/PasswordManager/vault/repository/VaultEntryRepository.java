package com.project.passwordmanager.PasswordManager.vault.repository;

import com.project.passwordmanager.PasswordManager.vault.entity.Vault;
import com.project.passwordmanager.PasswordManager.vault.entity.VaultEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VaultEntryRepository extends JpaRepository<VaultEntry, Long> {
    List<VaultEntry> findByVault(Vault vault);

    List<VaultEntry> findByVaultId(Long vaultId);

    List<VaultEntry> findByTitleContainingIgnoreCase(String title);

    boolean existsByVaultId(Long vaultId);
}
