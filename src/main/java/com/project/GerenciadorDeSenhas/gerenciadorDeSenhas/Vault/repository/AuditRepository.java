package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.repository;

import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditLog, Long> { }
