package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.service;

import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Domain.Vault;
import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.repository.VaultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class VaultEncryptionService {
    private final VaultRepository VaultRepository;
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final int SALT_LENGTH_BYTE = 32;
    private int PBKDF2_ITERATIONS = 200_000;

    public String encryptPassword(char[] password, Vault vault) {
        try {
            SecretKey key = deriveKey(vault);
            byte[] iv = generateIV();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] encrypted = cipher.doFinal(new String(password).getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public String hashVaultKey(String vaultKey) {
        try {
            byte[] salt = new byte[SALT_LENGTH_BYTE];
            new SecureRandom().nextBytes(salt);
            PBEKeySpec spec = new PBEKeySpec(vaultKey.toCharArray(), salt, PBKDF2_ITERATIONS, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            byte[] combined = new byte[salt.length + hash.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hash, 0, combined, salt.length, hash.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Erro ao gerar hash da chave do cofre", e);
        }
    }

    private SecretKey deriveKey(Vault vault) throws NoSuchAlgorithmException, InvalidKeySpecException {
        char[] vaultKey = vault.getVaultKey().toCharArray();
        byte[] salt = getOrCreateSalt(vault);
        KeySpec spec = new PBEKeySpec(vaultKey, salt, PBKDF2_ITERATIONS, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    private byte[] getOrCreateSalt(Vault vault) {
        if (vault.getEncryptionSalt() == null) {
            byte[] salt = new byte[SALT_LENGTH_BYTE];
            new SecureRandom().nextBytes(salt);
            vault.setEncryptionSalt(Base64.getEncoder().encodeToString(salt));
            VaultRepository.save(vault);
            return salt;
        }
        return Base64.getDecoder().decode(vault.getEncryptionSalt());
    }

    public boolean verifyVaultKey(String providedKey, String storedHash) {
        try {
            byte[] combined = Base64.getDecoder().decode(storedHash);
            byte[] salt = Arrays.copyOfRange(combined, 0, SALT_LENGTH_BYTE);
            byte[] expectedHash = Arrays.copyOfRange(combined, SALT_LENGTH_BYTE, combined.length);
            PBEKeySpec spec = new PBEKeySpec(providedKey.toCharArray(), salt, PBKDF2_ITERATIONS, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] providedHash = factory.generateSecret(spec).getEncoded();
            return MessageDigest.isEqual(providedHash, expectedHash);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao verificar chave do cofre", e);
        }
    }

    private byte[] generateIV() {
        byte[] iv = new byte[IV_LENGTH_BYTE];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}