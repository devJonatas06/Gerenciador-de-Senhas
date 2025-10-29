package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "VaultEntry")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VaultEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id", referencedColumnName = "id")
    private Vault vault;

    private String title;

    private String email;

    private String passwordEncrypted;

    private String url;

    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();



}
