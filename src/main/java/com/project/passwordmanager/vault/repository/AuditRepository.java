package com.project.passwordmanager.vault.repository;

import com.project.passwordmanager.vault.Domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditLog, Long> { }
