package com.davi.biblioteca.jpa.repository;

import com.davi.biblioteca.jpa.entity.EmprestimoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface EmprestimoJpaRepository extends JpaRepository<EmprestimoEntity, Long> {

    @Modifying
    @Query("UPDATE EmprestimoEntity e SET e.dataDevolucaoReal = :data WHERE e.id = :id")
    int registrarDevolucao(@Param("id") Long id, @Param("data") LocalDateTime data);
}