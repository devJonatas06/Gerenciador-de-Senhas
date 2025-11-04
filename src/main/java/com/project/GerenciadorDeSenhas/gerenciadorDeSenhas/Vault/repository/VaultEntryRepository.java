package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.repository;

import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Domain.VaultEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VaultEntryRepository extends JpaRepository<VaultEntry, Long> {

    List<VaultEntry> findByVaultId(Long vaultId);

    List<VaultEntry> findByTitleContainingIgnoreCase(String title);

    boolean existsByVaultId(Long vaultId);
}
