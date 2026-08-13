package com.davi.biblioteca.jpa.repository;

import com.davi.biblioteca.jpa.entity.MultaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MultaJpaRepository extends JpaRepository<MultaEntity, Long> {

    Optional<MultaEntity> findByEmprestimoId(Long emprestimoId);

    boolean existsByEmprestimoId(Long emprestimoId);

    List<MultaEntity> findByPagaFalseOrderByDataGeracaoDesc();
}