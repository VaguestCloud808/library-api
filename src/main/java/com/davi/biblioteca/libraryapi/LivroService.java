package com.davi.biblioteca.libraryapi;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LivroService {

    private final LivroRepository repository;

    public LivroService(LivroRepository repository) {
        this.repository = repository;
    }

    public Livro salvar(Livro livro) {
        validar(livro);
        return repository.salvar(livro);
    }

    public List<Livro> listar() {
        return repository.listar();
    }

    public Livro buscarPorId(Long id) {
        return repository.buscarPorId(id);
    }

    private void validar(Livro livro) {
        if (livro.getTitulo() == null || livro.getTitulo().isBlank()) {
            throw new IllegalArgumentException("Título é obrigatório");
        }
        if (livro.getAutor() == null || livro.getAutor().isBlank()) {
            throw new IllegalArgumentException("Autor é obrigatório");
        }
        if (livro.getQuantidadeTotal() == null || livro.getQuantidadeTotal() <= 0) {
            throw new IllegalArgumentException("Quantidade total deve ser maior que zero");
        }
        if (livro.getQuantidadeDisponivel() == null
                || livro.getQuantidadeDisponivel() < 0
                || livro.getQuantidadeDisponivel() > livro.getQuantidadeTotal()) {
            throw new IllegalArgumentException(
                    "Quantidade disponível deve estar entre 0 e a quantidade total");
        }
    }
}