package com.project.passwordmanager.PasswordManager.vault.exception;

public class VaultAccessDeniedException extends RuntimeException {
    public VaultAccessDeniedException(String message) {
        super(message);
    }
}