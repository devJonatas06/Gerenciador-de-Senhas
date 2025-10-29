package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.Vault.Domain;

import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.LoginGerenciadorDeSenha.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vaultLogin")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VaultLogin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private String name;

    private String vaultPassword;

}
