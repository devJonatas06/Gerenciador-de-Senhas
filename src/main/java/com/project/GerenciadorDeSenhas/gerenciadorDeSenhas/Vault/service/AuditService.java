package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.service;

import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Domain.AuditLog;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditRepository repository;

    public void recordAction(String email, String action) {
        AuditLog log = new AuditLog();
        log.setUserEmail(email);
        log.setAction(action);
        log.setTimestamp(LocalDateTime.now());
        repository.save(log);
    }
}
