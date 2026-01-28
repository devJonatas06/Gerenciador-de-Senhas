package com.project.passwordmanager.PasswordManager.vault.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.project.passwordmanager.PasswordManager.auth.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Entity
@Table(name = "vaults")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Vault {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;

    private String vaultName;

    @Column(nullable = false, length = 512)
    @JsonIgnore
    private String vaultKey;

    @JsonManagedReference // Adicione esta anotação
    @OneToMany(mappedBy = "vault", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VaultEntry> entries = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Column(nullable = true, length = 64)
    private String encryptionSalt;

    @PrePersist
    public void onCreate() {
        if (encryptionSalt == null) {
            byte[] salt = new byte[32];
            new SecureRandom().nextBytes(salt);
            encryptionSalt = Base64.getEncoder().encodeToString(salt);
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
}