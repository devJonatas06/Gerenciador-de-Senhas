package com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.repository;

import com.project.GerenciadorDeSenhas.gerenciadorDeSenhas.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

}