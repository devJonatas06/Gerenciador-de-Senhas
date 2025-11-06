package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.repository;

import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Domain.VaultLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VaultLoginRepository extends JpaRepository<VaultLogin, Long> {

    Optional<VaultLogin> findByUserId(Long userId);
    Optional<VaultLogin> findByEmail(String email);

    boolean existsByUserId(Long userId);
}
