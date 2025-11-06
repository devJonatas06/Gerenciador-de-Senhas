package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.repository;

import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.LoginGerenciadorDeSenha.domain.User;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Domain.Vault;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VaultRepository extends JpaRepository<Vault,Long> {
    List<Vault> findByUser(User user);
     List<Vault>findByUserId(Long id);
    boolean existsByUserId(Long id);

}
