package com.davi.biblioteca.jpa.repository;

import com.davi.biblioteca.jpa.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByEmail(String email);

    Optional<UsuarioEntity> findByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);
}