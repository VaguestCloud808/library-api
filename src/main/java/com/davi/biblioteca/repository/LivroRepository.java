package com.davi.biblioteca.repository;

import com.davi.biblioteca.model.Livro;

import java.util.List;
import java.util.Optional;

public interface LivroRepository {

    Livro salvar(Livro livro);

    List<Livro> listar();

    Optional<Livro> buscarPorId(Long id);

    void atualizar(Livro livro);
}
