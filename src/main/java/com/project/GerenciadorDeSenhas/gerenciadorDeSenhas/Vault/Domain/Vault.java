package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "vault")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Vault {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String service_name;

    @OneToMany
    @JoinTable(
            name = "users",
            joinColumns = @JoinColumn(name = "name")
    )
    private String userName;

    @OneToOne
    @JoinColumn(name = "vaultPassword")
    private String password;

    @OneToOne
    @JoinTable(
            name = "vaultLogin",
            joinColumns = @JoinColumn(name = "id")
    )
    private Long UserId;

    private LocalDateTime createdAt = LocalDateTime.now();




}
