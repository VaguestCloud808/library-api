package com.davi.biblioteca.libraryapi;
import java.util.List;
import java.util.Optional;

public interface LivroRepository {

    Livro salvar(Livro livro);

    List<Livro> listar();

    Optional<Livro> buscarPorId(Long id);
}
