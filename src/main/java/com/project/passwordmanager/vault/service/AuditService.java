package com.project.passwordmanager.vault.service;

import com.project.passwordmanager.vault.Domain.AuditLog;
import com.project.passwordmanager.vault.repository.AuditRepository;
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
