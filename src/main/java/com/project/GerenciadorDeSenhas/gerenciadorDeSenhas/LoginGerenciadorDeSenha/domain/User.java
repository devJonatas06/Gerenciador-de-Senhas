package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.LoginGerenciadorDeSenha.domain;

import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Domain.VaultLogin;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String email;
    private String password;

    @OneToOne(mappedBy = "vaultLogin", cascade = CascadeType.ALL)
    private VaultLogin vaultLogin;

}
