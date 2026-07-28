package com.davi.biblioteca.libraryapi;
import java.util.List;

public interface LivroRepository {

    Livro salvar(Livro livro);

    List<Livro> listar();

    Livro buscarPorId(Long id);
}
