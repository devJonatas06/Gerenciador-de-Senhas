package com.project.passwordmanager.vault.repository;

import com.project.passwordmanager.vault.entity.VaultLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VaultLoginRepository extends JpaRepository<VaultLogin, Long> {

    Optional<VaultLogin> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
