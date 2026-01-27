package com.project.passwordmanager.PasswordManager.vault.repository;

import com.project.passwordmanager.PasswordManager.vault.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditLog, Long> { }
