package com.davi.biblioteca.jpa.repository;

import com.davi.biblioteca.jpa.entity.LivroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LivroJpaRepository extends JpaRepository<LivroEntity, Long> {

    Optional<LivroEntity> findByIsbn(String isbn);

    @Modifying
    @Query("UPDATE LivroEntity l SET l.quantidadeDisponivel = l.quantidadeDisponivel - 1 " +
           "WHERE l.id = :id AND l.quantidadeDisponivel > 0")
    int decrementarQuantidadeDisponivel(@Param("id") Long id);

    @Modifying
    @Query("UPDATE LivroEntity l SET l.quantidadeDisponivel = l.quantidadeDisponivel + 1 " +
           "WHERE l.id = :id AND l.quantidadeDisponivel < l.quantidadeTotal")
    int incrementarQuantidadeDisponivel(@Param("id") Long id);
}