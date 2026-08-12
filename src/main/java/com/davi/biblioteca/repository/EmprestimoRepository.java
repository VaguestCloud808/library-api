package com.davi.biblioteca.repository;

import com.davi.biblioteca.model.Emprestimo;

import java.util.List;
import java.util.Optional;

public interface EmprestimoRepository {

    Emprestimo salvar(Emprestimo emprestimo);

    Optional<Emprestimo> buscarPorId(Long id);

    List<Emprestimo> listar();

    void registrarDevolucao(Long id, java.time.LocalDateTime dataDevolucao);
}
