package com.project.passwordmanager.PasswordManager.vault.repository;

import com.project.passwordmanager.PasswordManager.auth.entity.User;
import com.project.passwordmanager.PasswordManager.vault.entity.Vault;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VaultRepository extends JpaRepository<Vault,Long> {
    List<Vault> findByUser(User user);
    List<Vault> findByUserId(Long id);
    boolean existsByUserId(Long id);
}