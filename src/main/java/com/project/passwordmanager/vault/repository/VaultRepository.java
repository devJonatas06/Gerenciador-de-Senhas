package com.project.passwordmanager.vault.repository;

import com.project.passwordmanager.auth.domain.User;
import com.project.passwordmanager.vault.Domain.Vault;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VaultRepository extends JpaRepository<Vault,Long> {
    List<Vault> findByUser(User user);
     List<Vault>findByUserId(Long id);
    boolean existsByUserId(Long id);

}
