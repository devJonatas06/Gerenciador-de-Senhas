package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.service;

import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Domain.Vault;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Domain.VaultEntry;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Repository.VaultEntryRepository;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Repository.VaultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VaultEncryptionService {
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    
    private final VaultRepository vaultRepository;
    private final VaultEntryRepository vaultEntryRepository;
    
    public String encryptPassword(String password, Vault vault) {
        try {
            // Deriva chave do vault + master password do usuário
            SecretKey key = deriveKey(vault);
            byte[] iv = generateIV();
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            
            byte[] encrypted = cipher.doFinal(password.getBytes());
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    public String decryptPassword(String encryptedPassword, Vault vault) {
        try {
            SecretKey key = deriveKey(vault);
            byte[] combined = Base64.getDecoder().decode(encryptedPassword);
            
            byte[] iv = new byte[IV_LENGTH_BYTE];
            byte[] encrypted = new byte[combined.length - IV_LENGTH_BYTE];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTE);
            System.arraycopy(combined, IV_LENGTH_BYTE, encrypted, 0, encrypted.length);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
    
    private SecretKey deriveKey(Vault vault) {
        // Implementar derivação de chave usando vault key + user master password
        // Usar PBKDF2 ou similar
        return null; // Placeholder
    }
    
    private byte[] generateIV() {
        byte[] iv = new byte[IV_LENGTH_BYTE];
        new java.security.SecureRandom().nextBytes(iv);
        return iv;
    }
}