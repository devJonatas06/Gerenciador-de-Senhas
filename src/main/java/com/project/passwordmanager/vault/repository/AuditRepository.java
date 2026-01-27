package com.project.passwordmanager.vault.repository;

import com.project.passwordmanager.vault.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditLog, Long> { }
