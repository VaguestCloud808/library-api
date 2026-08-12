package com.davi.biblioteca.repository;

import com.davi.biblioteca.model.Multa;

import java.util.List;
import java.util.Optional;

public interface MultaRepository {

    Multa salvar(Multa multa);

    Optional<Multa> buscarPorId(Long id);

    Optional<Multa> buscarPorEmprestimoId(Long emprestimoId);

    boolean existePorEmprestimoId(Long emprestimoId);

    List<Multa> listar();

    List<Multa> listarEmAberto();

    Multa marcarComoPaga(Long id);
}